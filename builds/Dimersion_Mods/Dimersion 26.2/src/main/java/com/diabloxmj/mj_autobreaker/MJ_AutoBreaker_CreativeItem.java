package com.diabloxmj.mj_autobreaker;

import net.minecraft.item.ItemGroup;

// Classe utilitaire modulaire chargée d'insérer les éléments du module AutoBreaker dans l'onglet créatif personnalisé
public class MJ_AutoBreaker_CreativeItem {

    // Ajoute le bloc/item physique de l'AutoBreaker à la liste des entrées de l'onglet créatif fourni en paramètre
    public static void addEntries(ItemGroup.Entries entries) {
        entries.add(MJ_AutoBreaker_Blocks.AUTO_BREAKER_BLOCK);
        // Emplacement libre pour injecter d'autres futurs blocs ou objets de ce module si nécessaire
    }
}