package com.diabloxmj.mj_excavator;

import com.diabloxmj.mj_autobreaker.MJ_AutoBreaker_Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class MJ_Excavator_CreativeItem {
    public static void addEntries(ItemGroup.Entries entries) {
        String[] tiers = {"wood", "stone", "iron", "copper", "gold", "diamond", "netherite"};

        for (String tier : tiers) {
            // On récupère l'item par son ID enregistré dans le Registre Minecraft
            entries.add(Registries.ITEM.get(Identifier.of("dimersion", "mj_pickavator_" + tier)));
            entries.add(Registries.ITEM.get(Identifier.of("dimersion", "mj_shocavator_" + tier)));
            entries.add(Registries.ITEM.get(Identifier.of("dimersion", "mj_axecavator_" + tier)));
        }
    }
}