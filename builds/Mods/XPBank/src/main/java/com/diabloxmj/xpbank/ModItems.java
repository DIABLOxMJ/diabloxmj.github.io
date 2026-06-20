package com.diabloxmj.xpbank;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class ModItems {

    public static final Item SMALL_XP_VIAL = register("small_xp_vial", "small");
    public static final Item MEDIUM_XP_VIAL = register("medium_xp_vial", "medium");
    public static final Item LARGE_XP_VIAL = register("large_xp_vial", "large");

    public static final Item SMALL_SUPER_XP_VIAL = register("small_super_xp_vial", "small_super");
    public static final Item MEDIUM_SUPER_XP_VIAL = register("medium_super_xp_vial", "medium_super");
    public static final Item LARGE_SUPER_XP_VIAL = register("large_super_xp_vial", "large_super");


    public static void registerModItems() {
        Xpbank.LOGGER.info("Secure item registration for " + Xpbank.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
            content.add(SMALL_XP_VIAL);
            content.add(MEDIUM_XP_VIAL);
            content.add(LARGE_XP_VIAL);
            content.add(SMALL_SUPER_XP_VIAL);
            content.add(MEDIUM_SUPER_XP_VIAL);
            content.add(LARGE_SUPER_XP_VIAL);
        });
    }

    private static Item register(String name, String vialType) { // <--- OUVERTURE DE LA MÉTHODE
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Xpbank.id(name));
        Item.Settings settings = new Item.Settings().registryKey(key).maxCount(1);

        XpVialItem item = new XpVialItem(settings, vialType);
        return Registry.register(Registries.ITEM, key, item);
    }
}