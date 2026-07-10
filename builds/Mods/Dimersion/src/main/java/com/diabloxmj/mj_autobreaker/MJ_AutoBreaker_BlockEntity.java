package com.diabloxmj.mj_autobreaker;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// 1. Ajoute "implements NamedScreenHandlerFactory" en haut de la classe :
public class MJ_AutoBreaker_BlockEntity extends BlockEntity
        implements MJ_AutoBreaker_Inventory, SidedInventory, net.minecraft.screen.NamedScreenHandlerFactory {
    // Un inventaire de 19 slots (taille d'un dispenser)
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(19, ItemStack.EMPTY);

    public MJ_AutoBreaker_BlockEntity(BlockPos pos, BlockState state) {
        // Correction du nom de la variable globale enregistrée
        super(MJ_AutoBreaker_Blocks.AUTO_BREAKER_BLOCK_ENTITY_TYPE, pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    // --- SAUVEGARDE ET CHARGEMENT DE L'INVENTAIRE (CODECS SYSTEM 1.21+) ---
    @Override
    protected void writeData(WriteView view) {
        super.writeData(view); // Important pour la hiérarchie
        // On écrit l'inventaire dans la vue de sauvegarde
        Inventories.writeData(view, this.inventory);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view); // Important pour la hiérarchie
        // On lit l'inventaire depuis la vue de lecture
        Inventories.readData(view, this.inventory);
    }

    // --- LOGIQUE DE CASSAGE (TRY BREAK BLOCK) ---
    public void tryBreakBlock(ServerWorld world, BlockPos pos, Direction facing) {
        BlockPos targetPos = pos.offset(facing);
        BlockState targetState = world.getBlockState(targetPos);

        // SÉCURITÉ 0 : Si c'est de l'air ou un bloc incassable (Bedrock), on s'arrête
        if (targetState.isAir() || targetState.getHardness(world, targetPos) < 0) {
            return;
        }

        // Étape 1 : Récupérer l'outil dans le slot 0
        ItemStack tool = this.getStack(0);

        // SÉCURITÉ 1 : Si la machine n'a pas d'outil valide (slot vide ou item lambda), on refuse tout cassage
        if (tool.isEmpty() || tool.get(DataComponentTypes.TOOL) == null) {
            return;
        }

        // SÉCURITÉ 2 : Le Filtre Strict (Réévalué à chaque tick)
        // On vérifie si l'outil est adapté au bloc (gère la roche/les minerais)
        // OU si le bloc est mineable par cet outil spécifique (gère le bois, la terre, etc.)
        boolean isSuitable = tool.isSuitableFor(targetState);

        // En Minecraft 1.21+, le ToolComponent contient les règles de cassage (vitesse > 1.0F si c'est le bon outil)
        net.minecraft.component.type.ToolComponent toolComponent = tool.get(DataComponentTypes.TOOL);
        boolean isEffectiveSpeed = toolComponent != null && toolComponent.getSpeed(targetState) > 1.0F;

        // Si l'outil n'est ni requis/adapté (isSuitable) ET qu'il n'a pas de bonus de vitesse contre ce bloc (isEffectiveSpeed) :
        // Alors la machine refuse de casser ! (La houe refusera le bois/la terre, la pelle refusera le bois/la roche, etc.)
        if (!isSuitable && !isEffectiveSpeed) {
            return;
        }

        // --- Reste de ton code (LootWorldContext, breakBlock, etc.) inchangé ---
        net.minecraft.loot.context.LootWorldContext.Builder lootBuilder =
                new net.minecraft.loot.context.LootWorldContext.Builder(world)
                        .add(net.minecraft.loot.context.LootContextParameters.ORIGIN, Vec3d.ofCenter(targetPos))
                        .add(net.minecraft.loot.context.LootContextParameters.TOOL, tool);

        List<ItemStack> drops = targetState.getDroppedStacks(lootBuilder);

        world.breakBlock(targetPos, false);

        for (ItemStack drop : drops) {
            ItemStack restant = inputInStorage(drop);
            if (!restant.isEmpty()) {
                net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(
                        world, targetPos.getX() + 0.5, targetPos.getY() + 0.2, targetPos.getZ() + 0.5, restant
                );
                world.spawnEntity(itemEntity);
            }
        }

        if (!tool.isEmpty() && tool.isDamageable()) {
            tool.setDamage(tool.getDamage() + 1);
            if (tool.getDamage() >= tool.getMaxDamage()) {
                this.setStack(0, ItemStack.EMPTY);
                // Correction de la méthode de son pour les blocs / serveurs
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), net.minecraft.sound.SoundEvents.ENTITY_ITEM_BREAK, net.minecraft.sound.SoundCategory.BLOCKS, 0.8F, 0.8F, world.getRandom().nextLong());
            } else {
                this.markDirty();
            }
        }
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

    // 2. Ajoute la méthode qui donne un titre à la GUI (affiché en haut de l'écran)
    @Override
    public net.minecraft.text.Text getDisplayName() {
        // Crée une clé de traduction pour le nom du bloc (ex: "container.dimersion.autobreaker")
        return net.minecraft.text.Text.translatable("container.dimersion.mj_autobreaker");
    }

    // 3. Ajoute la méthode qui crée le lien avec notre ScreenHandler
    @Override
    public net.minecraft.screen.ScreenHandler createMenu(int syncId, net.minecraft.entity.player.PlayerInventory playerInventory, net.minecraft.entity.player.PlayerEntity player) {
        // On renvoie une nouvelle instance de notre ScreenHandler en lui passant l'inventaire de cette BlockEntity
        return new MJ_AutoBreaker_ScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        // Le secret : on ne renvoie JAMAIS le 0 dans cette liste.
        // Pour l'entonnoir, le slot 0 n'existe tout simplement PAS.
        return new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        // Si la machine (dir != null) essaie d'insérer, on interdit le slot 0
        if (dir != null && slot == 0) return false;
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        // Si la machine (dir != null) essaie d'extraire, on interdit le slot 0
        if (dir != null && slot == 0) return false;
        return true;
    }
}