package com.diabloxmj.xpbank;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import java.util.function.Consumer;

public class XpVialItem extends Item {

    private final String vialType;

    public XpVialItem(Settings settings, String vialType) {
        super(settings);
        this.vialType = vialType;
    }

    public int getMaxCapacity() {
        return switch (this.vialType) {
            case "small" -> ModConfig.INSTANCE.small_Vial_Max_Capacity;
            case "medium" -> ModConfig.INSTANCE.medium_Vial_Max_Capacity;
            case "large" -> ModConfig.INSTANCE.large_Vial_Max_Capacity;
            case "small_super" -> ModConfig.INSTANCE.small_super_Vial_Max_Capacity;
            case "medium_super" -> ModConfig.INSTANCE.medium_super_Vial_Max_Capacity;
            case "large_super" -> ModConfig.INSTANCE.large_super_Vial_Max_Capacity;
            default -> 500;
        };
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {

            ItemStack singleVial = itemStack.copyWithCount(1);
            int currentStoredXp = getStoredXp(singleVial);

            boolean isLevelMode = checkVialIsLevelMode();
            int playerCurrentPool = isLevelMode ? serverPlayer.experienceLevel : XpMathUtils.getPlayerTotalXp(serverPlayer);

            int currentMaxCapacity = getMaxCapacity();

            if (serverPlayer.isSneaking()) {
                if (playerCurrentPool <= 0) {
                    serverPlayer.sendMessage(Text.translatable("chat.xpbank.player_empty"), true);
                    return ActionResult.FAIL;
                }
                if (currentStoredXp >= currentMaxCapacity) {
                    serverPlayer.sendMessage(Text.translatable("chat.xpbank.vial_full"), true);
                    return ActionResult.FAIL;
                }

                int spaceLeftInVial = currentMaxCapacity - currentStoredXp;
                int toDeposit = Math.min(playerCurrentPool, spaceLeftInVial);

                if (isLevelMode) {
                    serverPlayer.setExperienceLevel(playerCurrentPool - toDeposit);
                } else {
                    XpMathUtils.setPlayerTotalXp(serverPlayer, playerCurrentPool - toDeposit);
                }

                setStoredXp(singleVial, currentStoredXp + toDeposit);
                serverPlayer.addExperience(0);
                serverPlayer.sendMessage(Text.translatable("chat.xpbank.deposit_success"), true);
            }
            else {
                if (currentStoredXp <= 0) {
                    serverPlayer.sendMessage(Text.translatable("chat.xpbank.vial_empty"), true);
                    return ActionResult.FAIL;
                }

                if (isLevelMode) {
                    serverPlayer.setExperienceLevel(serverPlayer.experienceLevel + currentStoredXp);
                } else {
                    XpMathUtils.setPlayerTotalXp(serverPlayer, playerCurrentPool + currentStoredXp);
                }

                setStoredXp(singleVial, 0);
                serverPlayer.addExperience(0);
                serverPlayer.sendMessage(Text.translatable("chat.xpbank.withdraw_success"), true);
            }

            if (itemStack.getCount() == 1) {
                player.setStackInHand(hand, singleVial);
            } else {
                itemStack.decrement(1);
                if (!player.getInventory().insertStack(singleVial)) {
                    player.dropItem(singleVial, false);
                }
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.SUCCESS;
    }

    public int getStoredXp(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            var nbt = customData.copyNbt();
            if (nbt.contains("stored_xp")) {
                return nbt.getInt("stored_xp").orElse(0);
            }
        }
        return 0;
    }

    public void setStoredXp(ItemStack stack, int amount) {
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbtComponent -> {
            var nbt = nbtComponent.copyNbt();
            nbt.putInt("stored_xp", amount);
            return NbtComponent.of(nbt);
        });

        float textureIndex = 0.0F;
        int currentMaxCapacity = getMaxCapacity();

        if (amount > 0) {
            if (amount >= currentMaxCapacity) {
                textureIndex = 3.0F;
            } else {
                float percentage = (float) amount / currentMaxCapacity;
                if (percentage >= 0.50F) {
                    textureIndex = 2.0F;
                } else {
                    textureIndex = 1.0F;
                }
            }
        }

        java.util.List<Float> floatList = java.util.List.of(textureIndex);
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(
                floatList,
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of()
        ));
    }

    private boolean checkVialIsLevelMode() {
        String type = switch (this.vialType) {
            case "small" -> ModConfig.INSTANCE.small_Vial_Xp_Type;
            case "medium" -> ModConfig.INSTANCE.medium_Vial_Xp_Type;
            case "large" -> ModConfig.INSTANCE.large_Vial_Xp_Type;
            case "small_super" -> ModConfig.INSTANCE.small_super_Vial_Xp_Type;
            case "medium_super" -> ModConfig.INSTANCE.medium_super_Vial_Xp_Type;
            case "large_super" -> ModConfig.INSTANCE.large_super_Vial_Xp_Type;
            default -> "POINTS";
        };
        return "LEVELS".equalsIgnoreCase(type);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, net.minecraft.component.type.TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        int current = getStoredXp(stack);
        String unit = checkVialIsLevelMode() ? " Lvl" : " Exp";

        textConsumer.accept(Text.literal("§a" + unit + ": " + current + " / " + getMaxCapacity()));
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}