package com.diabloxmj.mj_fogvision.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class MJ_FogVision_Mixin_Opacity {

    @Inject(method = "getOpacity", at = @At("HEAD"), cancellable = true)
    private void makeLavaTransparent(BlockState state, CallbackInfoReturnable<Integer> cir) {
        if (state.isOf(Blocks.LAVA)) {
            cir.setReturnValue(0);
        }
    }
}