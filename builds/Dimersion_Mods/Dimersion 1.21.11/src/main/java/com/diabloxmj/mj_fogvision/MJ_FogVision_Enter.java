package com.diabloxmj.mj_fogvision;

import net.fabricmc.api.ModInitializer;

public class MJ_FogVision_Enter implements ModInitializer {

    @Override
    public void onInitialize() {
        MJ_FogVision_Config.load();
    }
}