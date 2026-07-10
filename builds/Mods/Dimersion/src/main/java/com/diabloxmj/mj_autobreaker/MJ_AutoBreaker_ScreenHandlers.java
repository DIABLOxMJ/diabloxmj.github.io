package com.diabloxmj.mj_autobreaker;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

// Classe d'enregistrement technique permettant au moteur réseau de Minecraft de reconnaître l'écran de notre conteneur
public class MJ_AutoBreaker_ScreenHandlers {

    // Déclaration et instanciation du type de ScreenHandler lié à notre classe MJ_AutoBreaker_ScreenHandler
    // Utilise FeatureFlags.VANILLA_FEATURES requis par les versions récentes de Minecraft (1.21+)
    public static final ScreenHandlerType<MJ_AutoBreaker_ScreenHandler> AUTO_BREAKER_SCREEN_HANDLER_TYPE =
            new ScreenHandlerType<>(MJ_AutoBreaker_ScreenHandler::new, FeatureFlags.VANILLA_FEATURES);

    // Enregistre officiellement le ScreenHandler auprès du registre de Minecraft sous l'ID unique "dimersion:mj_autobreaker"
    public static void registerScreenHandlers() {
        Registry.register(Registries.SCREEN_HANDLER,
                Identifier.of("dimersion", "mj_autobreaker"),
                AUTO_BREAKER_SCREEN_HANDLER_TYPE);
    }
}