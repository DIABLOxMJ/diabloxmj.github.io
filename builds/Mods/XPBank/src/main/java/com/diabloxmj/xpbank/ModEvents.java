package com.diabloxmj.xpbank;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ModEvents {

    public static void registerEvents() {
        UseItemCallback.EVENT.register((PlayerEntity player, World world, Hand hand) -> {
            ItemStack itemStack = player.getStackInHand(hand);
            if (player.isSneaking() && itemStack.isOf(Items.GLASS_BOTTLE)) {
                if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
                    int playerTotalXp = XpMathUtils.getPlayerTotalXp(serverPlayer);
                    if (playerTotalXp < 8) {
                        serverPlayer.sendMessage(Text.translatable("chat.xpbank.not_enough_xp_sacrifice"), true);
                        return ActionResult.FAIL;
                    }
                    XpMathUtils.setPlayerTotalXp(serverPlayer, playerTotalXp - 8);
                    itemStack.decrement(1);
                    ItemStack expBottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
                    if (!serverPlayer.getInventory().insertStack(expBottle)) {
                        serverPlayer.dropItem(expBottle, false);
                    }

                    world.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                            SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.PLAYERS, 1.0F, 1.0F);

                    serverPlayer.sendMessage(Text.translatable("chat.xpbank.sacrifice_success"), true);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }
}