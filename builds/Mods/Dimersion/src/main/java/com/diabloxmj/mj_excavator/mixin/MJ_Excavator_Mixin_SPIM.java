package com.diabloxmj.mj_excavator.mixin;

import com.diabloxmj.mj_excavator.MJ_Excavator_Item;
import com.diabloxmj.mj_excavator.MJ_Mining_Helper;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class MJ_Excavator_Mixin_SPIM {
    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow protected ServerWorld world;

    private static boolean mj_isMining = false;

    // Signature explicite de la méthode cible pour éviter tout conflit d'injection
    @Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "net/minecraft/block/Block.onBroken(net/minecraft/world/World;net/minecraft/util/math/BlockPos;net/minecraft/block/BlockState;)V"), cancellable = true)
    private void onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (mj_isMining) return;

        ItemStack handStack = player.getMainHandStack();
        if (handStack.getItem() instanceof MJ_Excavator_Item) {
            mj_isMining = true;
            try {
                for (BlockPos extraPos : MJ_Mining_Helper.get3x3Blocks(pos, player)) {
                    BlockState state = world.getBlockState(extraPos);
                    if (handStack.isSuitableFor(state)) {
                        // Utilisation du gestionnaire d'interaction direct du joueur connecté
                        this.player.interactionManager.tryBreakBlock(extraPos);
                    }
                }
            } finally {
                mj_isMining = false;
            }
        }
    }
}