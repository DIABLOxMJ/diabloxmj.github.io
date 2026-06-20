package com.diabloxmj.xpbank;

import net.minecraft.server.network.ServerPlayerEntity;

public class XpMathUtils {

    public static int getPlayerTotalXp(ServerPlayerEntity player) {
        return getXpForLevel(player.experienceLevel) + Math.round(player.experienceProgress * player.getNextLevelExperience());
    }

    public static int getXpForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    public static void setPlayerTotalXp(ServerPlayerEntity player, int totalXp) {
        player.experienceLevel = 0;
        player.experienceProgress = 0.0F;
        player.totalExperience = 0;

        int xpLeft = totalXp;
        while (xpLeft > 0) {
            int xpToNextLevel = player.getNextLevelExperience();
            if (xpLeft >= xpToNextLevel) {
                player.experienceLevel++;
                xpLeft -= xpToNextLevel;
            } else {
                player.experienceProgress = (float) xpLeft / (float) xpToNextLevel;
                xpLeft = 0;
            }
        }
        player.totalExperience = totalXp;
    }
}