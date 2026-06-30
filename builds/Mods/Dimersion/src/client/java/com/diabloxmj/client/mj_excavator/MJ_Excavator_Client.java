package com.diabloxmj.client.mj_excavator;

import net.fabricmc.api.ClientModInitializer;

public class MJ_Excavator_Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Activation de l'affichage du cadrage 3x3
        MJ_Excavator_Renderer.register();
    }
}