package com.diabloxmj.mj_autobreaker;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class MJ_AutoBreaker_ScreenHandlers {
    // 1. Déclaration du type de ScreenHandler pour notre Auto-Breaker
    // Dans les versions récentes (1.21+), le constructeur de ScreenHandlerType requiert un FeatureSet (généralement FeatureFlags.VANILLA)
    public static final ScreenHandlerType<MJ_AutoBreaker_ScreenHandler> AUTO_BREAKER_SCREEN_HANDLER_TYPE =
            new ScreenHandlerType<>(MJ_AutoBreaker_ScreenHandler::new, FeatureFlags.VANILLA_FEATURES);

    // 2. Méthode d'initialisation pour enregistrer notre écran auprès du jeu
    public static void registerScreenHandlers() {
        Registry.register(Registries.SCREEN_HANDLER,
                Identifier.of("dimersion", "autobreaker"),
                AUTO_BREAKER_SCREEN_HANDLER_TYPE);
    }
}