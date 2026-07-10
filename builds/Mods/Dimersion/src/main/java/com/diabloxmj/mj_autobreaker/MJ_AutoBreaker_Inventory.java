package com.diabloxmj.mj_autobreaker;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public interface MJ_AutoBreaker_Inventory extends Inventory {

    DefaultedList<ItemStack> getItems();

    @Override
    default int size() {
        return getItems().size();
    }

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default ItemStack getStack(int slot) {
        return getItems().get(slot);
    }

    @Override
    default ItemStack removeStack(int slot, int count) {
        // On ne bloque plus rien ici ! On laisse le jeu gérer via les permissions du Slot
        ItemStack result = net.minecraft.inventory.Inventories.splitStack(getItems(), slot, count);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    default ItemStack removeStack(int slot) {
        if (slot == 0) return ItemStack.EMPTY;
        return net.minecraft.inventory.Inventories.removeStack(getItems(), slot);
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > stack.getMaxCount()) {
            stack.setCount(stack.getMaxCount());
        }
        markDirty();
    }

    @Override
    default void clear() {
        getItems().clear();
        markDirty();
    }

    @Override
    default void markDirty() {}

    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    int[] getAvailableSlots(Direction side);

    boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir);

    default boolean canExtract(int slot, ItemStack stack, Direction dir) {
        // Autorise tout le monde, SAUF le slot 0 quand dir n'est pas null
        return slot != 0;
    }
}