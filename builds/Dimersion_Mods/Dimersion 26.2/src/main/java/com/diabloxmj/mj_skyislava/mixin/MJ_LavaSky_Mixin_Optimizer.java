package com.diabloxmj.mj_skyislava.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidBlock.class)
public class MJ_LavaSky_Mixin_Optimizer {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!com.diabloxmj.mj_skyislava.MJ_LavaSky_Config.get().BestPerformance) {
            return;
        }
        if (state.isOf(Blocks.LAVA) && pos.getY() > 70) {
            ci.cancel();
        }
    }
}