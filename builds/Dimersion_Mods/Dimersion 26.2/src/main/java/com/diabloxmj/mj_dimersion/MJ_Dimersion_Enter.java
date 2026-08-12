package com.diabloxmj.mj_dimersion;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MJ_Dimersion_Enter implements ModInitializer {
    public static final String MOD_ID = "dimersion";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initialisation du mod Dimersion !");
        MJ_Dimersion_CreativeMenu.registerItemGroup();
    }
}