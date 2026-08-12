package com.diabloxmj.mj_gravityboots;

import com.diabloxmj.mj_dimersion.MJ_Dimersion_Enter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class MJ_GravityBoots_Items {

    public static Item GRAVITY_BOOTS;

    public static void registerModItems() {
        // 1. Définition de l'ID et de la clé d'item sans le tiret
        Identifier itemId = Identifier.of(MJ_Dimersion_Enter.MOD_ID, "mj_gravityboots");
        RegistryKey<Item> itemKey = RegistryKey.of(net.minecraft.registry.RegistryKeys.ITEM, itemId);

        // 2. Définition de la clé d'asset pointant sur ton fichier JSON personnalisé (dimersion:mj_gravityboots)
        RegistryKey<EquipmentAsset> GRAVITY_BOOTS_ASSET = RegistryKey.of(
                EquipmentAssetKeys.REGISTRY_KEY,
                Identifier.of(MJ_Dimersion_Enter.MOD_ID, "mj_gravityboots")
        );

        // 3. Enregistrement propre de l'item
        GRAVITY_BOOTS = Registry.register(
                Registries.ITEM,
                itemKey,
                new Item(new Item.Settings()
                        .registryKey(itemKey)
                        .maxDamage(EquipmentType.BOOTS.getMaxDamage(33))
                        .component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentType.BOOTS.getEquipmentSlot())
                                .equipSound(net.minecraft.sound.SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND)
                                .model(GRAVITY_BOOTS_ASSET) // Charge l'asset dimersion:mj_gravityboots
                                .build()
                        )
                )
        );
    }
}