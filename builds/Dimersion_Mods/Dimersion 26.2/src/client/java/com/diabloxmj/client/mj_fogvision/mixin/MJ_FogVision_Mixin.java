package com.diabloxmj.client.mj_fogvision.mixin;

import com.diabloxmj.mj_fogvision.MJ_FogVision_Config;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.fog.FogData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {
        "net.minecraft.client.render.fog.LavaFogModifier",
        "net.minecraft.client.render.fog.WaterFogModifier"
})

public class MJ_FogVision_Mixin {

    @Inject(method = "applyStartEndModifier", at = @At("TAIL"), remap = false)
    private void onApplyStartEnd(FogData data, Camera camera, ClientWorld clientWorld, float f, RenderTickCounter renderTickCounter, CallbackInfo ci) {
        if (MJ_FogVision_Config.get().NoFogViewer) {

            data.environmentalStart = 200.0F;
            data.environmentalEnd = 500.0F;
            data.renderDistanceStart = 200.0F;
            data.renderDistanceEnd = 500.0F;
        }
    }
}