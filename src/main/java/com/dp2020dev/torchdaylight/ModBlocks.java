package com.dp2020dev.torchdaylight;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Function;

public class ModBlocks {

    // --- Floor torches ---

    // Bright floor torch — full flame, light 14.
    public static final Block TORCH_BRIGHT = registerBlockOnly(
            "torch_bright",
            p -> new DaylightTorchBlock(ParticleTypes.FLAME, p),
            BlockBehaviour.Properties.of()
                    .noCollision()
                    .instabreak()
                    .lightLevel(state -> 14)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)
    );

    // "Off" floor torch — unlit look, no light. The daytime state.
    public static final Block TORCH_SMOULDERING = registerBlockOnly(
            "torch_smouldering",
            p -> new DaylightTorchBlock(ParticleTypes.SMALL_FLAME, p),
            BlockBehaviour.Properties.of()
                    .noCollision()
                    .instabreak()
                    .lightLevel(state -> 0)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)
    );

    // --- Wall torches ---

    public static final Block WALL_TORCH_BRIGHT = registerBlockOnly(
            "wall_torch_bright",
            p -> new DaylightWallTorchBlock(ParticleTypes.FLAME, p),
            BlockBehaviour.Properties.of()
                    .noCollision()
                    .instabreak()
                    .lightLevel(state -> 14)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)
    );

    public static final Block WALL_TORCH_SMOULDERING = registerBlockOnly(
            "wall_torch_smouldering",
            p -> new DaylightWallTorchBlock(ParticleTypes.SMALL_FLAME, p),
            BlockBehaviour.Properties.of()
                    .noCollision()
                    .instabreak()
                    .lightLevel(state -> 0)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)
    );

    // --- The Sunrise Torch item ---
    // A standing-and-wall item: places TORCH_BRIGHT on the ground, WALL_TORCH_BRIGHT on a wall.
    // Registered AFTER the blocks above so both are available to reference.
    public static final Item SUNRISE_TORCH_ITEM = registerTorchItem("torch_bright");

    // Registers a block WITHOUT an inventory item.
    private static Block registerBlockOnly(
            String name,
            Function<BlockBehaviour.Properties, Block> factory,
            BlockBehaviour.Properties properties
    ) {
        ResourceKey<Block> key = ResourceKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(TorchDaylight.MOD_ID, name)
        );
        Block block = factory.apply(properties.setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    // Registers the torch item as a StandingAndWallBlockItem linking floor + wall variants.
    private static Item registerTorchItem(String name) {
        ResourceKey<Item> itemKey = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(TorchDaylight.MOD_ID, name)
        );
        StandingAndWallBlockItem item = new StandingAndWallBlockItem(
                TORCH_BRIGHT,           // placed on the ground
                WALL_TORCH_BRIGHT,      // placed against a wall
                Direction.DOWN,         // attachment direction (matches vanilla torch)
                new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)
        );
        return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }

    public static void initialize() {
        TorchDaylight.LOGGER.info("Registering blocks for {}", TorchDaylight.MOD_ID);
    }
}