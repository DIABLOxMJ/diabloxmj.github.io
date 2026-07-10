package com.diabloxmj.mj_autobreaker;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;

import static com.diabloxmj.mj_autobreaker.MJ_AutoBreaker_Blocks.AUTO_BREAKER_BLOCK;

public class MJ_AutoBreaker_Enter implements ModInitializer {

    @Override
    public void onInitialize() {
        MJ_AutoBreaker_Blocks.registerModBlocks();
        MJ_AutoBreaker_ScreenHandlers.registerScreenHandlers();

        // À mettre dans ton initialiseur principal (onInitialize) :
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.add(AUTO_BREAKER_BLOCK);
        });
    }
}