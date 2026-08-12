package com.diabloxmj.mj_autobreaker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MJ_AutoBreaker_BlockEntity extends BlockEntity
        implements MJ_AutoBreaker_Inventory, SidedInventory, net.minecraft.screen.NamedScreenHandlerFactory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(19, ItemStack.EMPTY);

    public MJ_AutoBreaker_BlockEntity(BlockPos pos, BlockState state) {
        super(MJ_AutoBreaker_Blocks.AUTO_BREAKER_BLOCK_ENTITY_TYPE, pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.inventory);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, this.inventory);
    }

    // --- LOGIQUE DE CASSAGE (TRY BREAK BLOCK) ---
    public void tryBreakBlock(ServerWorld world, BlockPos pos, Direction facing) {
        if (world.isClient()) return;

        BlockState blockState = getCachedState();

        // SÉCURITÉ REDSTONE
        if (world.isReceivingRedstonePower(pos) && blockState.get(MJ_AutoBreaker_Block.TRIGGERED)) {

            BlockPos targetPos = pos.offset(facing);
            BlockState targetState = world.getBlockState(targetPos);

            if (targetState.isAir() || targetState.getHardness(world, targetPos) < 0) {
                world.scheduleBlockTick(pos, blockState.getBlock(), 20);
                return;
            }

            // --- VERIFICATION ET DESTRUCTION DE SECURITE (CORRECTIF TIERS) ---
            ItemStack tool = this.getStack(0);

            // SÉCURITÉ A : Le slot est-il vide ou l'objet n'a pas de composant d'outil ?
            if (tool.isEmpty() || tool.get(DataComponentTypes.TOOL) == null) {
                world.scheduleBlockTick(pos, blockState.getBlock(), 20);
                return;
            }

            net.minecraft.component.type.ToolComponent toolComponent = tool.get(DataComponentTypes.TOOL);

            // RESOLUTION DE LA FAILLE :
            // On demande au composant de l'outil s'il a le niveau requis pour faire DROP le bloc.
            // Une pioche en bois ou en fer sur de l'obsidienne renverra FALSE ici.
            if (toolComponent == null || !toolComponent.isCorrectForDrops(targetState)) {
                // L'outil est trop faible (ex: bois sur obsidienne/diamant), on refuse de casser !
                world.scheduleBlockTick(pos, blockState.getBlock(), 20);
                return;
            }

            // SÉCURITÉ B : On s'assure que l'outil est un minimum efficace sur ce bloc
            if (toolComponent.getSpeed(targetState) <= 1.0F) {
                world.scheduleBlockTick(pos, blockState.getBlock(), 20);
                return;
            }

            // Génération des loots prévus pour vérification
            LootWorldContext.Builder lootBuilder =
                    new LootWorldContext.Builder(world)
                            .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(targetPos))
                            .add(LootContextParameters.TOOL, tool);

            List<ItemStack> drops = targetState.getDroppedStacks(lootBuilder);

            // SIMULATION DE STOCKAGE
            if (!hasSpaceForDrops(drops)) {
                world.scheduleBlockTick(pos, blockState.getBlock(), 20);
                return;
            }

            // --- DESTRUCTION EFFECTIVE ---
            world.breakBlock(targetPos, false);

            for (ItemStack drop : drops) {
                ItemStack restant = inputInStorage(drop);
                if (!restant.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(
                            world, targetPos.getX() + 0.5, targetPos.getY() + 0.2, targetPos.getZ() + 0.5, restant
                    );
                    world.spawnEntity(itemEntity);
                }
            }

            // --- USURE DE L'OUTIL ---
            if (!tool.isEmpty() && tool.isDamageable()) {
                tool.setDamage(tool.getDamage() + 1);
                if (tool.getDamage() >= tool.getMaxDamage()) {
                    this.setStack(0, ItemStack.EMPTY);
                    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 0.8F, 0.8F, world.getRandom().nextLong());
                } else {
                    this.markDirty();
                }
            }

            world.scheduleBlockTick(pos, blockState.getBlock(), 20);

        } else {
            if (blockState.get(MJ_AutoBreaker_Block.TRIGGERED)) {
                world.setBlockState(pos, blockState.with(MJ_AutoBreaker_Block.TRIGGERED, false), Block.NOTIFY_LISTENERS);
            }
        }
    }

    private boolean hasSpaceForDrops(List<ItemStack> drops) {
        DefaultedList<ItemStack> virtualInv = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        for (int i = 1; i < this.size(); i++) {
            virtualInv.set(i, this.getStack(i).copy());
        }

        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            ItemStack currentDrop = drop.copy();

            for (int i = 1; i < virtualInv.size(); i++) {
                ItemStack slotStack = virtualInv.get(i);
                if (!slotStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(slotStack, currentDrop)) {
                    int maxCount = slotStack.getMaxCount();
                    int espace = maxCount - slotStack.getCount();
                    if (espace > 0) {
                        int aAjouter = Math.min(espace, currentDrop.getCount());
                        slotStack.increment(aAjouter);
                        currentDrop.decrement(aAjouter);
                    }
                }
                if (currentDrop.isEmpty()) break;
            }

            if (!currentDrop.isEmpty()) {
                for (int i = 1; i < virtualInv.size(); i++) {
                    if (virtualInv.get(i).isEmpty()) {
                        virtualInv.set(i, currentDrop.copy());
                        currentDrop.setCount(0);
                        break;
                    }
                }
            }

            if (!currentDrop.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private ItemStack inputInStorage(ItemStack stack) {
        ItemStack copie = stack.copy();

        for (int i = 1; i < this.size(); i++) {
            ItemStack slotStack = this.getStack(i);
            if (!slotStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(slotStack, copie)) {
                int espaceDisponible = slotStack.getMaxCount() - slotStack.getCount();
                if (espaceDisponible > 0) {
                    int aAjouter = Math.min(espaceDisponible, copie.getCount());
                    slotStack.increment(aAjouter);
                    copie.decrement(aAjouter);
                    this.markDirty();
                }
            }
            if (copie.isEmpty()) return ItemStack.EMPTY;
        }

        for (int i = 1; i < this.size(); i++) {
            if (this.getStack(i).isEmpty()) {
                this.setStack(i, copie);
                this.markDirty();
                return ItemStack.EMPTY;
            }
        }

        return copie;
    }

    @Override
    public net.minecraft.text.Text getDisplayName() {
        return net.minecraft.text.Text.translatable("container.dimersion.mj_autobreaker");
    }

    @Override
    public net.minecraft.screen.ScreenHandler createMenu(int syncId, net.minecraft.entity.player.PlayerInventory playerInventory, net.minecraft.entity.player.PlayerEntity player) {
        return new MJ_AutoBreaker_ScreenHandler(syncId, playerInventory, this);
    }

    // --- PIPELINES POUR LES HOPPERS (SIDED INVENTORY) ---
    @Override
    public int[] getAvailableSlots(Direction side) {
        // CORRECTION DE L'ENTONNOIR : On rend TOUS les slots (0 à 18) accessibles par l'extérieur
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        // Si un entonnoir pousse un objet dans la machine :
        if (dir != null) {
            boolean isOutil = stack.get(DataComponentTypes.TOOL) != null;

            if (slot == 0) {
                // Règle 1 : Dans le slot 0, on accepte UNIQUEMENT les outils de rechange
                return isOutil;
            } else {
                // Règle 2 : Dans les slots de stockage (1 à 18), on REFUSE catégoriquement les outils (Blacklist)
                return !isOutil;
            }
        }
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        // Empêche un entonnoir en dessous d'aspirer l'outil de travail du slot 0
        if (dir != null && slot == 0) return false;
        return true;
    }
}