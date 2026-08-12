package com.diabloxmj.client.mj_autobreaker;

import com.diabloxmj.mj_autobreaker.MJ_AutoBreaker_ScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.fabricmc.api.ClientModInitializer;

public class MJ_AutoBreaker_Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(MJ_AutoBreaker_ScreenHandlers.AUTO_BREAKER_SCREEN_HANDLER_TYPE, MJ_AutoBreaker_Screen::new);
    }
}