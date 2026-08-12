package com.diabloxmj.client.mj_xpbottle; // Déclare le sous-package réservé aux éléments s'exécutant uniquement sur l'ordinateur du joueur (Client)

import com.diabloxmj.mj_xpbottle.MJ_XPBottle_Config; // Importe le fichier de configuration central pour y injecter les données du serveur
import com.diabloxmj.mj_xpbottle.network.MJ_XPBottle_Sync_Payload; // Importe le plan de construction de notre paquet réseau personnalisé
import net.fabricmc.api.ClientModInitializer; // Importe l'interface obligatoire de Fabric pour initialiser le côté client d'un mod
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking; // Importe l'API réseau de Fabric dédiée aux récepteurs côté client

// Classe principale client qui implémente ClientModInitializer : Minecraft l'appelle au chargement des graphismes/moteur de jeu local
public class MJ_XPBottle_Client implements ClientModInitializer {
    // Méthode s'exécutant automatiquement au démarrage du jeu du joueur (avant d'arriver sur le menu principal)
    @Override
    public void onInitializeClient() {

        // Étape 1 : Le client tend l'oreille et s'enregistre auprès du réseau global de Minecraft.
        // Dès qu'un paquet portant l'ID secret de "ConfigSyncPayload" (MJ_XPBottle_Enter:config_sync) arrive, ce bloc de code s'allume.
        ClientPlayNetworking.registerGlobalReceiver(MJ_XPBottle_Sync_Payload.ID, (payload, context) -> {
            // Étape 2 : Sécurité Threading. Les paquets réseau arrivent sur un fil (Thread) secondaire en arrière-plan.
            // La méthode context.client().execute(...) ordonne de transférer l'action sur le fil principal (Main Thread) du jeu.
            // C'est obligatoire en Java pour éviter que le jeu ne plante ou ne se fige en modifiant la mémoire à la volée.
            context.client().execute(() -> {
                // Étape 3 : Écrasement des variables. Le client intercepte le paquet envoyé par le serveur et extrait les valeurs.
                // Il remplace immédiatement ses propres variables locales temporaires par celles dictées à chaud par le serveur.
                MJ_XPBottle_Config.INSTANCE.XPBottle_lvl1_Max_Capacity = payload.lvl1Max();
                MJ_XPBottle_Config.INSTANCE.XPBottle_lvl2_Max_Capacity = payload.lvl2Max();
                MJ_XPBottle_Config.INSTANCE.XPBottle_lvl3_Max_Capacity = payload.lvl3Max();
                MJ_XPBottle_Config.INSTANCE.XPBottle_lvl4_Max_Capacity = payload.lvl4Max();
                MJ_XPBottle_Config.INSTANCE.XPBottle_lvl5_Max_Capacity = payload.lvl5Max();
                MJ_XPBottle_Config.INSTANCE.XPBottle_lvl6_Max_Capacity = payload.lvl6Max();
            });
        });
    }
}