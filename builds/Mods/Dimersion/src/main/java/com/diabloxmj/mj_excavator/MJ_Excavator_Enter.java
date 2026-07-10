package com.diabloxmj.mj_excavator;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MJ_Excavator_Enter implements ModInitializer {
    public static final String MOD_ID = "excavator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        MJ_Excavator_Config.load();
        MJ_Excavator_Items.registerModItems();
    }
}