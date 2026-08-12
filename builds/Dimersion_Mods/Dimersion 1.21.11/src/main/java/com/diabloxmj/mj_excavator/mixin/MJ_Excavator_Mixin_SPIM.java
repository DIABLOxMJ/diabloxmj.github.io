package com.diabloxmj.mj_excavator.mixin;

import com.diabloxmj.mj_excavator.MJ_Excavator_Item;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow; // Note: Remets l'import @Shadow standard de ton projet
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayerInteractionManager.class)
public class MJ_Excavator_Mixin_SPIM {
    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow protected ServerWorld world;

    private static final ThreadLocal<Boolean> IS_MINING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (IS_MINING.get()) return;

        ItemStack handStack = player.getMainHandStack();
        if (handStack.getItem() instanceof MJ_Excavator_Item) {
            ItemStack mainHandStack = this.player.getMainHandStack();
            IS_MINING.set(true);
            try {
                // Raycast pour la face visée
                HitResult hitResult = player.raycast(5.0D, 1.0F, false);
                Direction side = HitResult.Type.BLOCK == hitResult.getType() ? ((BlockHitResult) hitResult).getSide() : Direction.UP;

                // On récupère la liste des 9 blocs (centre inclus) alignés sur la vraie face visée
                List<BlockPos> targetBlocks = get3x3PositionsFromSide(pos, side);
                boolean brokenAny = false;

                // 2. Si le bloc principal est cassé avec succès, on applique DIRECTEMENT 1 point de dégât.
                // Le "item -> {}" est un callback vide ultra-sécurisé qui évite les bugs réseau au milieu du mixin.
                // Cette méthode prend nativement en compte l'enchantement Solidité (Unbreaking) !
                mainHandStack.damage(1, this.world, this.player, item -> {});

                for (BlockPos extraPos : targetBlocks) {
                    if (player.canModifyAt(world, extraPos)) {
                        BlockState state = world.getBlockState(extraPos);

                        // CONDITION AJUSTÉE :
                        // Cas 1 : C'est le bloc du milieu (extraPos est égal à pos) -> On le casse d'office !
                        // Cas 2 : C'est un bloc autour -> On le casse UNIQUEMENT si l'outil est adapté.
                        if (extraPos.equals(pos) || (handStack.isSuitableFor(state) && !state.isAir())) {
                            world.breakBlock(extraPos, true, player);
                            brokenAny = true;
                        }
                    }
                }

                // Si on a cassé au moins un bloc dans la zone (ou si le centre était le bon),
                // on valide l'événement pour de bon
                if (brokenAny) {
                    cir.setReturnValue(true);
                }
            } finally {
                IS_MINING.set(false);
            }
        }
    }

    private List<BlockPos> get3x3PositionsFromSide(BlockPos center, Direction side) {
        List<BlockPos> positions = new ArrayList<>();
        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();

        // Si on mine un sol ou un plafond -> Grille horizontale (X et Z)
        if (side == Direction.UP || side == Direction.DOWN) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    positions.add(new BlockPos(x + dx, y, z + dz));
                }
            }
        }
        // Si on mine un mur face Nord ou Sud -> Grille verticale (X et Y)
        else if (side == Direction.NORTH || side == Direction.SOUTH) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    positions.add(new BlockPos(x + dx, y + dy, z));
                }
            }
        }
        // Si on mine un mur face Est ou Ouest -> Grille verticale (Z et Y)
        else if (side == Direction.EAST || side == Direction.WEST) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    positions.add(new BlockPos(x, y + dy, z + dz));
                }
            }
        }
        return positions;
    }
}