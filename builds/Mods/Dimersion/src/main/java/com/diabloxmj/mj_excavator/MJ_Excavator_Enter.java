package com.diabloxmj.mj_excavator;

import net.fabricmc.api.ModInitializer;

public class MJ_Excavator_Enter implements ModInitializer {

    @Override
    public void onInitialize() {
        MJ_Excavator_Config.load();
        MJ_Excavator_Items.registerModItems();
    }
}