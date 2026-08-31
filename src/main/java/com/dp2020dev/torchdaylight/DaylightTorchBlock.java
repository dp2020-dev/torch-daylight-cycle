package com.dp2020dev.torchdaylight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DaylightTorchBlock extends TorchBlock {

    private static final int SKYLIGHT_THRESHOLD = 8;

    public DaylightTorchBlock(SimpleParticleType flameParticle, BlockBehaviour.Properties properties) {
        super(flameParticle, properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        boolean shouldDim = level.isBrightOutside() && skyLight >= SKYLIGHT_THRESHOLD;
        boolean isBright = this == ModBlocks.TORCH_BRIGHT;

        if (isBright && shouldDim) {
            swapTo(level, pos, ModBlocks.TORCH_SMOULDERING);
        } else if (!isBright && !shouldDim) {
            swapTo(level, pos, ModBlocks.TORCH_BRIGHT);
        }
    }

    private void swapTo(ServerLevel level, BlockPos pos, Block target) {
        level.setBlockAndUpdate(pos, target.defaultBlockState());
    }
}