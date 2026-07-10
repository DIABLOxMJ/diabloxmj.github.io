package com.diabloxmj.mj_excavator;

import com.diabloxmj.mj_dimersion.MJ_Dimersion_Enter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.function.Function;

import static net.minecraft.item.Items.register;

public class MJ_Excavator_Items {

    // La méthode corrigée pour Minecraft 1.21.11+
    public static <T extends Item> T register(String name, Function<Item.Settings, T> itemFactory, Item.Settings settings) {
        Identifier id = Identifier.of(MJ_Dimersion_Enter.MOD_ID, name);

        // 1. On crée la clé de registre officielle pour cet item
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

        // 2. CORRECTION : On injecte la clé directement dans les settings de l'item AVANT de le créer
        settings.registryKey(key);

        // 3. On fabrique l'item (qui ne plantera plus puisqu'il a désormais son ID !)
        T item = itemFactory.apply(settings);

        // 4. On l'enregistre en utilisant sa clé
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void registerModItems() {
        // Enregistrement des PICKAVATORS (Pioches)
        registerPickavator("wood", "wood_durability", 2.0F);
        registerPickavator("stone", "stone_durability", 4.0F);
        registerPickavator("iron", "iron_durability", 6.0F);
        registerPickavator("copper", "copper_durability", 5.0F);
        registerPickavator("gold", "gold_durability", 12.0F);
        registerPickavator("diamond", "diamond_durability", 8.0F);
        registerPickavator("netherite", "netherite_durability", 9.0F);

        // Enregistrement des SHOCAVATORS (Pelles)
        registerShocavator("wood", "wood_durability", 2.0F);
        registerShocavator("stone", "stone_durability", 4.0F);
        registerShocavator("iron", "iron_durability", 6.0F);
        registerShocavator("copper", "copper_durability", 5.0F);
        registerShocavator("gold", "gold_durability", 12.0F);
        registerShocavator("diamond", "diamond_durability", 8.0F);
        registerShocavator("netherite", "netherite_durability", 9.0F);

        // Enregistrement des AXECAVATORS (Haches)
        registerAxecavator("wood", "wood_durability", 2.0F);
        registerAxecavator("stone", "stone_durability", 4.0F);
        registerAxecavator("iron", "iron_durability", 6.0F);
        registerAxecavator("copper", "copper_durability", 5.0F);
        registerAxecavator("gold", "gold_durability", 12.0F);
        registerAxecavator("diamond", "diamond_durability", 8.0F);
        registerAxecavator("netherite", "netherite_durability", 9.0F);
    }

    private static Item registerPickavator(String tierName, String configKey, float speed) {
        int maxDamage = MJ_Excavator_Config.getDurability(configKey);
        int enchantability = 10; // Valeur requise pour la table d'enchantement !

        RegistryEntryList<net.minecraft.block.Block> blocks = RegistryEntryList.of(Registries.BLOCK, BlockTags.PICKAXE_MINEABLE);
        ToolComponent.Rule rule = ToolComponent.Rule.ofAlwaysDropping(blocks, speed);
        ToolComponent toolComponent = new ToolComponent(List.of(rule), 1.0F, 1, false);

        // AJOUT DE .enchantable(enchantability) ICI
        register("mj_pickavator_" + tierName, MJ_Pickavator_Item::new,
                new Item.Settings().maxDamage(maxDamage).component(DataComponentTypes.TOOL, toolComponent).enchantable(enchantability));
        return Items.register("mj_pickavator_" + tierName, MJ_Pickavator_Item::new);
    }

    private static Item registerShocavator(String tierName, String configKey, float speed) {
        int maxDamage = MJ_Excavator_Config.getDurability(configKey);
        int enchantability = 10;

        RegistryEntryList<net.minecraft.block.Block> blocks = RegistryEntryList.of(Registries.BLOCK, BlockTags.SHOVEL_MINEABLE);
        ToolComponent.Rule rule = ToolComponent.Rule.ofAlwaysDropping(blocks, speed);
        ToolComponent toolComponent = new ToolComponent(List.of(rule), 1.0F, 1, false);

        // AJOUT DE .enchantable(enchantability) ICI
        register("mj_shocavator_" + tierName, MJ_Shocavator_Item::new,
                new Item.Settings().maxDamage(maxDamage).component(DataComponentTypes.TOOL, toolComponent).enchantable(enchantability));
        return Items.register("mj_shocavator_" + tierName, MJ_Pickavator_Item::new);
    }

    private static Item registerAxecavator(String tierName, String configKey, float speed) {
        int maxDamage = MJ_Excavator_Config.getDurability(configKey);
        int enchantability = 10;

        RegistryEntryList<net.minecraft.block.Block> blocks = RegistryEntryList.of(Registries.BLOCK, BlockTags.AXE_MINEABLE);
        ToolComponent.Rule rule = ToolComponent.Rule.ofAlwaysDropping(blocks, speed);
        ToolComponent toolComponent = new ToolComponent(List.of(rule), 1.0F, 1, false);

        // AJOUT DE .enchantable(enchantability) ICI
        register("mj_axecavator_" + tierName, MJ_Axecavator_Item::new,
                new Item.Settings().maxDamage(maxDamage).component(DataComponentTypes.TOOL, toolComponent).enchantable(enchantability));
        return Items.register("mj_axecavator_" + tierName, MJ_Pickavator_Item::new);
    }
}