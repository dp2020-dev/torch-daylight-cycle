import { system, world } from "@minecraft/server";

const BLOCK_ID = "sunsettorch:sunset_torch";
const STATE = "sunsettorch:daylight";
const FACE_STATE = "minecraft:block_face";
const WALL_STATE = "sunsettorch:wall"; // side the wall is on: none | north | south | east | west

// --- Daylight swap ---------------------------------------------------------

// Java rule: dim when level.isBrightOutside() && skyLight >= 8.
// Bedrock time of day: 0 = sunrise, 6000 = noon, 12000 = sunset, 13000+ = night.
// DAY_START/DAY_END approximate isBrightOutside(); tune these against the Java mod.
const DAY_START = 0;
const DAY_END = 12000;

function isDaylightFor(block) {
  if (block.dimension.id !== "minecraft:overworld") return false;

  const t = world.getTimeOfDay();
  if (t < DAY_START || t >= DAY_END) return false;

  // Approximates Java's skyLight >= 8 check: torch must have open sky above it.
  // TODO: use a real sky-light query if the Script API offers one; handle rain/thunder.
  const top = block.dimension.getTopmostBlock({ x: block.x, z: block.z });
  return !top || top.location.y <= block.y;
}

// Works out which side the torch is mounted on by looking for a solid neighbour,
// so the lean never depends on how the game reports the clicked face.
const SIDES = {
  north: { x: 0, z: -1 },
  south: { x: 0, z: 1 },
  east: { x: 1, z: 0 },
  west: { x: -1, z: 0 },
};
const OPPOSITE = { north: "south", south: "north", east: "west", west: "east" };

const DEBUG = false; // dev builds only: prints what the placement code decided

function debug(msg) {
  if (DEBUG) world.sendMessage(`[torch] ${msg}`);
}

function isWallBlock(n) {
  if (!n || n.isAir || n.isLiquid) return false;
  if (n.isSolid) return true;
  // isSolid can be unreliable for some blocks; fall back to "anything non-replaceable".
  return !/grass|fern|flower|torch|sapling|vine|snow_layer|rail|button|lever|carpet/.test(n.typeId);
}

function detectWall(block) {
  const face = block.permutation.getState(FACE_STATE);
  if (face === "up" || face === undefined) return { wall: "none", face, solid: [] };
  const solid = [];
  for (const [side, d] of Object.entries(SIDES)) {
    let n;
    try {
      n = block.offset({ x: d.x, y: 0, z: d.z });
    } catch (e) {
      continue;
    }
    if (isWallBlock(n)) solid.push(side);
  }
  let wall;
  if (solid.length === 1) wall = solid[0];
  else if (solid.length === 0) wall = OPPOSITE[face] || "none"; // fall back to the clicked face
  else if (solid.includes(OPPOSITE[face])) wall = OPPOSITE[face];
  else if (solid.includes(face)) wall = face;
  else wall = solid[0];
  return { wall, face, solid };
}

function place(block) {
  try {
    const r = detectWall(block);
    debug(`face=${r.face} solid=[${r.solid.join(",")}] -> wall=${r.wall}`);
    let perm = block.permutation.withState(WALL_STATE, r.wall);
    perm = perm.withState(STATE, isDaylightFor(block));
    block.setPermutation(perm);
  } catch (e) {
    debug(`place failed: ${e}`);
  }
}

function update(block) {
  const wanted = isDaylightFor(block);
  if (block.permutation.getState(STATE) !== wanted) {
    block.setPermutation(block.permutation.withState(STATE, wanted));
  }
}

// --- Flame particles -------------------------------------------------------

// Bedrock custom blocks can't emit particles themselves, so torches register
// here when they place/tick and one shared timer spawns the flames.
const PARTICLE_INTERVAL = 4; // ticks between flame spawns
const SMOKE_EVERY = 6; // one smoke puff per this many flame spawns (~24 ticks)
const PARTICLE_RANGE = 24; // only animate torches this close to a player

const FLAME = "minecraft:basic_flame_particle";
const SMOULDER_SMOKE_CHANCE = 0.07; // per spawn cycle: a wisp roughly every 3 seconds
const SMOKE = "minecraft:basic_smoke_particle";

const tracked = new Map();

function track(block) {
  const key = `${block.dimension.id}|${block.x}|${block.y}|${block.z}`;
  if (!tracked.has(key)) {
    tracked.set(key, { dimension: block.dimension, x: block.x, y: block.y, z: block.z });
  }
}

// Wall torches lean away from the wall, so the flame sits 2.7/16 toward the wall
// from the block centre and about 0.87 up (matches the 22.5 degree geometry).
const WALL_OFFSETS = {
  north: { x: 0, z: -0.17 },
  south: { x: 0, z: 0.17 },
  east: { x: 0.17, z: 0 },
  west: { x: -0.17, z: 0 },
};

function flamePosition(entry, permutation) {
  const off = WALL_OFFSETS[permutation.getState(WALL_STATE)];
  return {
    x: entry.x + 0.5 + (off ? off.x : 0),
    y: entry.y + (off ? 0.87 : 0.7),
    z: entry.z + 0.5 + (off ? off.z : 0),
  };
}

function near(player, entry) {
  if (player.dimension.id !== entry.dimension.id) return false;
  const dx = player.location.x - entry.x;
  const dy = player.location.y - entry.y;
  const dz = player.location.z - entry.z;
  return dx * dx + dy * dy + dz * dz <= PARTICLE_RANGE * PARTICLE_RANGE;
}

system.runInterval(() => {
  if (tracked.size === 0) return;
  const players = world.getAllPlayers();
  if (players.length === 0) return;

  for (const [key, entry] of tracked) {
    if (!players.some((p) => near(p, entry))) continue;

    let block;
    try {
      block = entry.dimension.getBlock({ x: entry.x, y: entry.y, z: entry.z });
    } catch {
      continue; // chunk not loaded
    }
    if (!block) continue;
    if (block.typeId !== BLOCK_ID) {
      tracked.delete(key); // torch was broken or replaced
      continue;
    }
    // Bright: flame + rare smoke. Smouldering (daylight): no flame, just an
    // occasional dark wisp rising from the ash.
    const smouldering = block.permutation.getState(STATE) === true;
    const pos = flamePosition(entry, block.permutation);
    entry.count = (entry.count || 0) + 1;
    try {
      if (smouldering) {
        if (Math.random() < SMOULDER_SMOKE_CHANCE) entry.dimension.spawnParticle(SMOKE, pos);
      } else {
        entry.dimension.spawnParticle(FLAME, pos);
        if (entry.count % SMOKE_EVERY === 0) entry.dimension.spawnParticle(SMOKE, pos);
      }
    } catch {
      // ignore particles that fail to spawn this tick
    }
  }
}, PARTICLE_INTERVAL);

// --- Registration ----------------------------------------------------------

system.beforeEvents.startup.subscribe(({ blockComponentRegistry }) => {
  blockComponentRegistry.registerCustomComponent("sunsettorch:daylight_swap", {
    onPlace(event) {
      place(event.block);
      track(event.block);
    },
    onTick(event) {
      update(event.block);
      track(event.block);
    },
  });
});
