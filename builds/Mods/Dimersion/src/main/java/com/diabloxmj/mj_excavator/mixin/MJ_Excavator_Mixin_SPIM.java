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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayerInteractionManager.class)
public class MJ_Excavator_Mixin_SPIM {
    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow protected ServerWorld world;

    // Utilisation d'un flag statique pour bloquer la récursion
    private static final ThreadLocal<Boolean> IS_MINING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (IS_MINING.get()) return;

        ItemStack handStack = player.getMainHandStack();
        if (handStack.getItem() instanceof MJ_Excavator_Item) {
            IS_MINING.set(true);
            try {
                // Pour obtenir la face exacte sans Raycast foireux :
                // On récupère le mouvement du joueur ou on utilise la face "évidente"
                // Mais pour faire simple et efficace, on récupère la face visée par le joueur
                // via la logique interne de Minecraft si disponible, ou on utilise le bloc lui-même.

                Direction side = getFacingDirection();

                for (BlockPos extraPos : get3x3PositionsFromSide(pos, side)) {
                    if (player.canModifyAt(world, extraPos)) {
                        BlockState state = world.getBlockState(extraPos);
                        if (handStack.isSuitableFor(state) && !state.isAir()) {
                            // On casse manuellement sans relancer tryBreakBlock pour éviter la boucle
                            world.breakBlock(extraPos, true, player);
                        }
                    }
                }
            } finally {
                IS_MINING.set(false);
            }
        }
    }

    private Direction getFacingDirection() {
        // La méthode la plus fiable : on regarde la direction du regard du joueur
        return player.getHorizontalFacing().getOpposite();
    }
}