package com.dp2020dev.torchdaylight;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Function;

public class ModBlocks {

    // Bright torch — full flame, light level 14. This one HAS an inventory item (craftable "Sunrise Torch").
    public static final Block TORCH_BRIGHT = registerWithItem(
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

    // "Off" torch — unlit look, no light. The daytime state.
    public static final Block TORCH_SMOULDERING = registerBlockOnly(
            "torch_smouldering",
            p -> new DaylightTorchBlock(ParticleTypes.SMALL_FLAME, p),
            BlockBehaviour.Properties.of()
                    .noCollision()
                    .instabreak()
                    .lightLevel(state -> 0)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
    );

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

    // Registers a block AND a matching BlockItem for the inventory.
    private static Block registerWithItem(
            String name,
            Function<BlockBehaviour.Properties, Block> factory,
            BlockBehaviour.Properties properties
    ) {
        // Register the block first (reuse the block-only path).
        Block block = registerBlockOnly(name, factory, properties);

        // Now register a matching item.
        ResourceKey<Item> itemKey = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(TorchDaylight.MOD_ID, name)
        );
        BlockItem blockItem = new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        return block;
    }

    public static void initialize() {
        TorchDaylight.LOGGER.info("Registering blocks for {}", TorchDaylight.MOD_ID);
    }
}