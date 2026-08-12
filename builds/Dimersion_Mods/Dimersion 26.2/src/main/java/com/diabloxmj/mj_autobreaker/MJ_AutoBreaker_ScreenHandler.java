package com.diabloxmj.mj_autobreaker;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class MJ_AutoBreaker_ScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    // 1. LE CONSTRUCTEUR POUR LE CLIENT (Appelé automatiquement par le jeu)
    public MJ_AutoBreaker_ScreenHandler(int syncId, PlayerInventory playerInventory) {
        // Il DOIT appeler le grand constructeur en lui passant un inventaire vide de 19 slots
        this(syncId, playerInventory, new net.minecraft.inventory.SimpleInventory(19));
    }

    // 2. LE GRAND CONSTRUCTEUR (Pour le serveur)
    public MJ_AutoBreaker_ScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(MJ_AutoBreaker_ScreenHandlers.AUTO_BREAKER_SCREEN_HANDLER_TYPE, syncId);
        checkSize(inventory, 19);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        // Slot 0 : Outil
        this.addSlot(new Slot(inventory, 0, 26, 36) {
            @Override
            public boolean canInsert(ItemStack stack) {
                // Seuls les outils sont acceptés
                return stack.get(DataComponentTypes.TOOL) != null;
            }

            @Override
            public boolean canTakeItems(PlayerEntity player) {
                // Le joueur a TOUJOURS le droit de prendre l'outil
                return true;
            }
        });

        // Slots 1 à 18 : Grille 3x6
        int index = 1;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {
                this.addSlot(new Slot(inventory, index, 62 + col * 18, 18 + row * 18));
                index++;
            }
        }

        // Inventaire joueur (27 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // LA HOTBAR (Les 9 slots manquants qui causaient le crash !)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    // Est-ce que le joueur peut utiliser ce bloc sans tricher (distance requise) ?
    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    // Gestion du Shift+Clic pour transférer les items proprement sans faire crasher le jeu
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            // Si le clic vient d'un slot de l'AutoBreaker (0 à 18)
            if (invSlot < 19) {
                // On essaie de déplacer vers l'inventaire joueur (slots 19 à 55 environ)
                if (!this.insertItem(originalStack, 19, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Si on déplace depuis le joueur vers le slot 0 (Outil)
                if (!this.insertItem(originalStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return newStack;
    }
}