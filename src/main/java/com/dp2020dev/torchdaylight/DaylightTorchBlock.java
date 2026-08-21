package com.dp2020dev.torchdaylight;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DaylightTorchBlock extends TorchBlock {

    public DaylightTorchBlock(SimpleParticleType flameParticle, BlockBehaviour.Properties properties) {
        super(flameParticle, properties);
    }
}