package com.diabloxmj.mj_skyislava;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MJ_LavaSky_Enter implements ModInitializer {

    @Override
    public void onInitialize() {
        MJ_LavaSky_Config.load();
    }
}