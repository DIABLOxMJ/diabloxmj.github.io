package com.diabloxmj.mj_gravityboots;

import net.minecraft.item.ItemGroup;

// Classe utilitaire modulaire chargée d'insérer les éléments du module AutoBreaker dans l'onglet créatif personnalisé
public class MJ_GravityBoots_CreativeItem {

    // Ajoute le bloc/item physique de l'AutoBreaker à la liste des entrées de l'onglet créatif fourni en paramètre
    public static void addEntries(ItemGroup.Entries entries) {
        entries.add(MJ_GravityBoots_Items.GRAVITY_BOOTS);
        // Emplacement libre pour injecter d'autres futurs blocs ou objets de ce module si nécessaire
    }
}