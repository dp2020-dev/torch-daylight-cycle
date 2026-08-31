package com.dp2020dev.torchdaylight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DaylightTorchBlock extends TorchBlock {

    private static final int SKYLIGHT_THRESHOLD = 8;
    private static final int CHECK_INTERVAL = 20; // ~1 second between checks

    public DaylightTorchBlock(SimpleParticleType flameParticle, BlockBehaviour.Properties properties) {
        super(flameParticle, properties);
    }

    // When placed (or loaded), start the self-scheduling check loop.
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 1);
        }
    }

    // Fires on each scheduled tick: check light, swap if needed, then reschedule.
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        boolean shouldDim = level.isBrightOutside() && skyLight >= SKYLIGHT_THRESHOLD;
        boolean isBright = this == ModBlocks.TORCH_BRIGHT;

        if (isBright && shouldDim) {
            swapTo(level, pos, ModBlocks.TORCH_SMOULDERING);
            return; // new block's onPlace restarts the loop
        } else if (!isBright && !shouldDim) {
            swapTo(level, pos, ModBlocks.TORCH_BRIGHT);
            return; // new block's onPlace restarts the loop
        }

        // No change needed — schedule the next check (small jitter avoids mass-sync spikes).
        int jitter = random.nextInt(5);
        level.scheduleTick(pos, this, CHECK_INTERVAL + jitter);
    }

    private void swapTo(ServerLevel level, BlockPos pos, Block target) {
        level.setBlockAndUpdate(pos, target.defaultBlockState());
    }
}