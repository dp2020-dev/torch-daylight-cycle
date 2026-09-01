package com.dp2020dev.torchdaylight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DaylightWallTorchBlock extends WallTorchBlock {

    private static final int SKYLIGHT_THRESHOLD = 8;
    private static final int CHECK_INTERVAL = 20;

    public DaylightWallTorchBlock(SimpleParticleType flameParticle, BlockBehaviour.Properties properties) {
        super(flameParticle, properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        boolean shouldDim = level.isBrightOutside() && skyLight >= SKYLIGHT_THRESHOLD;
        boolean isBright = this == ModBlocks.WALL_TORCH_BRIGHT;

        if (isBright && shouldDim) {
            swapTo(level, pos, state, ModBlocks.WALL_TORCH_SMOULDERING);
            return;
        } else if (!isBright && !shouldDim) {
            swapTo(level, pos, state, ModBlocks.WALL_TORCH_BRIGHT);
            return;
        }

        int jitter = random.nextInt(5);
        level.scheduleTick(pos, this, CHECK_INTERVAL + jitter);
    }

    private void swapTo(ServerLevel level, BlockPos pos, BlockState oldState, Block target) {
        BlockState newState = target.defaultBlockState();
        newState = newState.setValue(WallTorchBlock.FACING, oldState.getValue(WallTorchBlock.FACING));
        level.setBlockAndUpdate(pos, newState);
    }
}