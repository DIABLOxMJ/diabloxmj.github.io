package com.diabloxmj.mj_autobreaker; // Déclare le dossier (package) où se trouve ce fichier de code

import net.fabricmc.api.ModInitializer; // Importe l'interface Fabric qui définit les classes d'initialisation de mod
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents; // Importe l'API Fabric permettant de modifier les onglets créatifs existants
import net.minecraft.item.ItemGroups; // Importe la liste des catégories créatives Vanilla (Blocs fonctionnels, Matériaux, etc.)

// Importation statique : permet d'utiliser directement la variable \"AUTO_BREAKER_BLOCK\" sans réécrire \"MJ_AutoBreaker_Blocks.AUTO_BREAKER_BLOCK\" à chaque fois
import static com.diabloxmj.mj_autobreaker.MJ_AutoBreaker_Blocks.AUTO_BREAKER_BLOCK;

public class MJ_AutoBreaker_Enter implements ModInitializer { // Déclare la classe qui s'occupe d'allumer le module de l'AutoBreaker

    @Override
    public void onInitialize() { // Cette méthode s'exécute automatiquement une seule fois au démarrage de Minecraft

        // 1. Enregistre le bloc physique, son item et sa BlockEntity dans le grand catalogue de Minecraft
        MJ_AutoBreaker_Blocks.registerModBlocks();

        // 2. Enregistre l'existence technique de l'écran (GUI/Menu) auprès du jeu
        MJ_AutoBreaker_ScreenHandlers.registerScreenHandlers();

        // 3. Injection "Bonus" dans l'inventaire Créatif de base de Minecraft
        // On écoute l'événement de modification de l'onglet \"FUNCTIONAL\" (Blocs fonctionnels de Minecraft, là où il y a les pistons et les hoppers)
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.add(AUTO_BREAKER_BLOCK); // On y glisse notre bloc pour que le joueur puisse le trouver en Vanilla sans l'onglet personnalisé
        });
    }
}