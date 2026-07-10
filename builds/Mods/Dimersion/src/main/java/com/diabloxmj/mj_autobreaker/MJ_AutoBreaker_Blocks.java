package com.diabloxmj.mj_autobreaker;

import com.diabloxmj.mj_dimersion.MJ_Dimersion_Enter;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class MJ_AutoBreaker_Blocks {

    public static Block AUTO_BREAKER_BLOCK;
    public static BlockEntityType<MJ_AutoBreaker_BlockEntity> AUTO_BREAKER_BLOCK_ENTITY_TYPE;

    public static void registerModBlocks() {
        Identifier blockId = Identifier.of(MJ_Dimersion_Enter.MOD_ID, "mj_autobreaker");
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, blockId);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, blockId);

        // 1. Création et enregistrement du Bloc physique (avec des propriétés similaires au Dispenser / Four)
        AUTO_BREAKER_BLOCK = Registry.register(
                Registries.BLOCK, blockKey,
                new MJ_AutoBreaker_Block(AbstractBlock.Settings.copy(Blocks.OBSERVER)
                        .registryKey(blockKey)
                        .strength(2.5F, 200.0F) // 2.5F = Dureté (vitesse de cassage), 200.0F = Résistance aux explosions
                )
        );

        // 2. Enregistrement de l'Item associé pour pouvoir le tenir en main et le poser
        Registry.register(Registries.ITEM, itemKey, new BlockItem(AUTO_BREAKER_BLOCK, new Item.Settings().registryKey(itemKey)));

        // 3. Liaison officielle via la classe FabricBlockEntityTypeBuilder
        AUTO_BREAKER_BLOCK_ENTITY_TYPE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(MJ_Dimersion_Enter.MOD_ID, "mj_autobreaker"),
                net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(
                        MJ_AutoBreaker_BlockEntity::new,
                        AUTO_BREAKER_BLOCK
                ).build()
        );
    }
}