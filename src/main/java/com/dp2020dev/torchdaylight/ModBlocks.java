package com.dp2020dev.torchdaylight;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Function;

public class ModBlocks {

    // Bright torch — full flame, light level 14 (the nighttime / dark-area state)
    public static final Block TORCH_BRIGHT = register(
            "torch_bright",
            p -> new DaylightTorchBlock(ParticleTypes.FLAME, p),
            BlockBehaviour.Properties.of()
                    .noCollision()
                    .instabreak()
                    .lightLevel(state -> 14)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
    );

    // Smouldering torch — low ember, light level 7 (the daylight state)
    public static final Block TORCH_SMOULDERING = register(
            "torch_smouldering",
            p -> new DaylightTorchBlock(ParticleTypes.SMOKE, p),
            BlockBehaviour.Properties.of()
                    .noCollision()
                    .instabreak()
                    .lightLevel(state -> 7)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
    );

    private static Block register(
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

    public static void initialize() {
        TorchDaylight.LOGGER.info("Registering blocks for {}", TorchDaylight.MOD_ID);
    }
}