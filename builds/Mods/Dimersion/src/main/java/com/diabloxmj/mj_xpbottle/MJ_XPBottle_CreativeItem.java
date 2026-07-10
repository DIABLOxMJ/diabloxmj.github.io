package com.diabloxmj.mj_xpbottle;

import net.minecraft.item.ItemGroup;

import static com.diabloxmj.mj_xpbottle.MJ_XPBottle_Item_Mod.XPBottle_lvl1;
import static com.diabloxmj.mj_xpbottle.MJ_XPBottle_Item_Mod.XPBottle_lvl2;
import static com.diabloxmj.mj_xpbottle.MJ_XPBottle_Item_Mod.XPBottle_lvl3;
import static com.diabloxmj.mj_xpbottle.MJ_XPBottle_Item_Mod.XPBottle_lvl4;
import static com.diabloxmj.mj_xpbottle.MJ_XPBottle_Item_Mod.XPBottle_lvl5;
import static com.diabloxmj.mj_xpbottle.MJ_XPBottle_Item_Mod.XPBottle_lvl6;


public class MJ_XPBottle_CreativeItem {
    // Ajoute cette méthode pour permettre au menu global de récupérer tes items
    public static void addEntries(ItemGroup.Entries entries) {
        entries.add(XPBottle_lvl1);
        entries.add(XPBottle_lvl2);
        entries.add(XPBottle_lvl3);
        entries.add(XPBottle_lvl4);
        entries.add(XPBottle_lvl5);
        entries.add(XPBottle_lvl6);
    }
}
