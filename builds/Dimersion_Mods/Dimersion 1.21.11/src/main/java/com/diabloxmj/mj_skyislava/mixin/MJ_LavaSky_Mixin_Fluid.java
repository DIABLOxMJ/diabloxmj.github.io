package com.diabloxmj.mj_skyislava.mixin;

import net.minecraft.fluid.LavaFluid;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LavaFluid.class)
public class MJ_LavaSky_Mixin_Fluid {
    @Inject(method = "getTickRate", at = @At("HEAD"), cancellable = true)
    private void onGetTickRate(WorldView world, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(90);
    }
}
