package com.diabloxmj.mj_xpbottle;

import com.diabloxmj.mj_dimersion.MJ_Dimersion_Enter;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents; // Importe l'outil Fabric permettant d'injecter des objets dans l'inventaire créatif
import net.minecraft.item.Item; // Importe la classe de base représentant un objet brut dans Minecraft
import net.minecraft.item.ItemGroups; // Importe la liste des onglets d'inventaire créatif natifs (Combats, Outils, etc.)
import net.minecraft.registry.Registries; // Importe la liste de tous les grands registres de Minecraft (Blocs, Items, Entités...)
import net.minecraft.registry.Registry; // Importe l'outil d'inscription dans les registres
import net.minecraft.registry.RegistryKey; // Importe la structure de clé d'identification unique pour le registre
import net.minecraft.registry.RegistryKeys; // Importe la table contenant les types de clés valides (Clé d'item, de bloc, etc.)

public class MJ_XPBottle_Item_Mod {

    // Déclaration et instanciation des fioles standards en leur injectant leur étiquette descriptive interne
    public static final Item XPBottle_lvl1 = register("mj_xpbottle_lvl1", "lvl1");
    public static final Item XPBottle_lvl2 = register("mj_xpbottle_lvl2", "lvl2");
    public static final Item XPBottle_lvl3 = register("mj_xpbottle_lvl3", "lvl3");
    public static final Item XPBottle_lvl4 = register("mj_xpbottle_lvl4", "lvl4");
    public static final Item XPBottle_lvl5 = register("mj_xpbottle_lvl5", "lvl5");
    public static final Item XPBottle_lvl6 = register("mj_xpbottle_lvl6", "lvl6");

    // Méthode publique appelée par la classe principale pour rendre nos items accessibles et les trier en mode Créatif
    public static void registerModItems() {
        MJ_Dimersion_Enter.LOGGER.info("Secure item registration for " + MJ_Dimersion_Enter.MOD_ID); // Envoie un message de suivi de sécurité dans la console

        // Événement permettant de modifier les entrées de l'onglet créatif "Ingrédients" de Minecraft
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
            content.add(XPBottle_lvl1); // Ajoute la fiole niveau 1 dans l'onglet
            content.add(XPBottle_lvl2); // Ajoute la fiole niveau 2 dans l'onglet
            content.add(XPBottle_lvl3); // Ajoute la fiole niveau 3 dans l'onglet
            content.add(XPBottle_lvl4); // Ajoute la fiole niveau 4 dans l'onglet
            content.add(XPBottle_lvl5); // Ajoute la fiole niveau 5 dans l'onglet
            content.add(XPBottle_lvl6); // Ajoute la fiole niveau 6 dans l'onglet
        });
    }

    // Méthode privée interne centralisant la création technique de la clé et l'enregistrement de l'item dans le moteur
    private static Item register(String name, String bottleType) {
        // Étape 1 : Crée une clé de registre unique combinant la nature (ITEM) et l'ID complet (ex: xpbottle:mj_xpbottle_lvl1)
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, MJ_XPBottle_Enter.id(name));

        // Étape 2 : Configure les paramètres de l'item (on lui attache sa clé et on limite sa taille maximale de pile à 1)
        Item.Settings settings = new Item.Settings().registryKey(key).maxCount(1);

        // Étape 3 : Crée l'instance physique de notre fiole personnalisée en lui transmettant sa configuration et son type
        com.diabloxmj.mj_xpbottle.MJ_XPBottle_Item item = new com.diabloxmj.mj_xpbottle.MJ_XPBottle_Item(settings, bottleType);

        // Étape 4 : Grave l'item dans le marbre du registre officiel des items de Minecraft et le retourne
        return Registry.register(Registries.ITEM, key, item);
    }

}