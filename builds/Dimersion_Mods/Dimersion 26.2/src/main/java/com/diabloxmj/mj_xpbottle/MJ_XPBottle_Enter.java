package com.diabloxmj.mj_xpbottle; // Définit l'adresse logique (package) du fichier dans l'arborescence

import com.diabloxmj.mj_dimersion.MJ_Dimersion_Enter;
import net.fabricmc.api.ModInitializer; // Importe l'interface obligatoire de Fabric pour initialiser un mod

import com.diabloxmj.mj_xpbottle.network.MJ_XPBottle_Sync_Payload; // Importe la structure de notre paquet réseau personnalisé
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry; // Importe le registre réseau pour déclarer nos paquets
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents; // Importe les déclencheurs (events) de connexion réseau
import net.minecraft.util.Identifier; // Importe l'outil de création d'ID de Minecraft (namespace:path)

// Classe principale qui implémente ModInitializer : elle contient le commutateur de démarrage
public class MJ_XPBottle_Enter implements ModInitializer {

    // Méthode s'exécutant automatiquement au démarrage du serveur de jeu (Solo ou Dédié)
    @Override
    public void onInitialize() {
        MJ_XPBottle_Config.load(); // Déclenche la lecture et le chargement du fichier de configuration JSON
        MJ_XPBottle_Item_Mod.registerModItems(); // Déclenche l'enregistrement de l'intégralité de nos fioles d'XP
        MJ_XPBottle_Event.registerEvents(); // Déclenche l'écoute de nos événements personnalisés

        // Enregistre officiellement notre paquet "ConfigSyncPayload" dans le registre réseau de type S2C (Server-To-Client)
        PayloadTypeRegistry.playS2C().register(com.diabloxmj.mj_xpbottle.network.MJ_XPBottle_Sync_Payload.ID, MJ_XPBottle_Sync_Payload.CODEC);

        // Événement s'activant dès qu'un joueur passe l'étape de connexion et apparaît physiquement dans le monde
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // Le serveur crée une enveloppe réseau (Payload) contenant toutes ses propres valeurs de capacité maximale
            MJ_XPBottle_Sync_Payload payload = new MJ_XPBottle_Sync_Payload(
                    MJ_XPBottle_Config.INSTANCE.XPBottle_lvl1_Max_Capacity,
                    MJ_XPBottle_Config.INSTANCE.XPBottle_lvl2_Max_Capacity,
                    MJ_XPBottle_Config.INSTANCE.XPBottle_lvl3_Max_Capacity,
                    MJ_XPBottle_Config.INSTANCE.XPBottle_lvl4_Max_Capacity,
                    MJ_XPBottle_Config.INSTANCE.XPBottle_lvl5_Max_Capacity,
                    MJ_XPBottle_Config.INSTANCE.XPBottle_lvl6_Max_Capacity
            );
            // Le serveur expédie l'enveloppe sur le réseau à destination unique du joueur qui vient d'entrer
            sender.sendPacket(payload);
        });
    }

    // Méthode utilitaire permettant de générer rapidement un identifiant au format standardisé "dimersion:nom_de_l_element"
    public static Identifier id(String path) {
        return Identifier.of(MJ_Dimersion_Enter.MOD_ID, path); // Construit l'identifiant avec le namespace de ton mod
    }
}