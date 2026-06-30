package com.diabloxmj.mj_skyislava;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MJ_LavaSky_Enter implements ModInitializer {
    public static final String MOD_ID = "lavasky";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initialisation du mod LavaSky !");
        MJ_LavaSky_Config.load();
    }
}