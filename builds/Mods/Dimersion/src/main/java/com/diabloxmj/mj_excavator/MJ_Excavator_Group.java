package com.diabloxmj.mj_excavator;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MJ_Excavator_Group {
    // 1. Déclaration de la clé unique pour notre groupe d'onglets
    public static final RegistryKey<ItemGroup> EXCAVATOR_ITEM_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            Identifier.of(MJ_Excavator_Enter.MOD_ID, "item_group")
    );

    // 2. Création et configuration de l'onglet
    public static final ItemGroup EXCAVATOR_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(Registries.ITEM.get(Identifier.of(MJ_Excavator_Enter.MOD_ID, "pickavator_netherite")))) // Icône de l'onglet (Pioche Netherite)
            .displayName(Text.translatable("itemGroup." + MJ_Excavator_Enter.MOD_ID + ".item_group")) // Nom affiché (géré via le fichier lang)
            .entries((context, entries) -> {
                // Liste ordonnée des tiers pour un affichage propre dans l'inventaire
                String[] tiers = {"wood", "stone", "iron", "copper", "gold", "diamond", "netherite"};

                // On ajoute toutes les pioches
                for (String tier : tiers) {
                    entries.add(Registries.ITEM.get(Identifier.of(MJ_Excavator_Enter.MOD_ID, "pickavator_" + tier)));
                }
                // On ajoute toutes les pelles
                for (String tier : tiers) {
                    entries.add(Registries.ITEM.get(Identifier.of(MJ_Excavator_Enter.MOD_ID, "shocavator_" + tier)));
                }
                // On ajoute toutes les haches
                for (String tier : tiers) {
                    entries.add(Registries.ITEM.get(Identifier.of(MJ_Excavator_Enter.MOD_ID, "axecavator_" + tier)));
                }
            })
            .build();

    // 3. Enregistrement officiel de l'onglet dans le jeu
    public static void registerItemGroup() {
        Registry.register(Registries.ITEM_GROUP, EXCAVATOR_ITEM_GROUP_KEY, EXCAVATOR_ITEM_GROUP);
    }
}