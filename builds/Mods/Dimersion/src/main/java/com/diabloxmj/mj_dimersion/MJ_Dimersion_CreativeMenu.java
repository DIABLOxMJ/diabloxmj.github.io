package com.diabloxmj.mj_dimersion;

import com.diabloxmj.mj_autobreaker.MJ_AutoBreaker_Blocks;
import com.diabloxmj.mj_autobreaker.MJ_AutoBreaker_CreativeItem;
import com.diabloxmj.mj_excavator.MJ_Excavator_CreativeItem;
import com.diabloxmj.mj_xpbottle.MJ_XPBottle_CreativeItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MJ_Dimersion_CreativeMenu {

    // 1. Déclaration de la clé unique (très important pour que Minecraft l'identifie)
    public static final RegistryKey<ItemGroup> DIMERSION_ITEM_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            Identifier.of(MJ_Dimersion_Enter.MOD_ID, "item_group")
    );

    // 2. Création et configuration de l'onglet
    public static final ItemGroup DIMERSION_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(MJ_AutoBreaker_Blocks.AUTO_BREAKER_BLOCK))
            .displayName(Text.translatable("itemGroup.dimersion.item_group"))
            .entries((context, entries) -> {
                // Appels modulaires
                MJ_AutoBreaker_CreativeItem.addEntries(entries);
                MJ_Excavator_CreativeItem.addEntries(entries);
                MJ_XPBottle_CreativeItem.addEntries(entries);
            })
            .build();

    // 3. La méthode appelée dans MJ_Dimersion_Enter pour valider l'existence de l'onglet
    public static void registerItemGroup() {
        // C'est CETTE ligne précise qui manquait pour lier la clé à l'onglet dans le moteur du jeu :
        Registry.register(Registries.ITEM_GROUP, DIMERSION_ITEM_GROUP_KEY, DIMERSION_ITEM_GROUP);
    }
}