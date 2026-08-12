package com.diabloxmj.mj_excavator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.ArrayList;
import java.util.List;

public class MJ_Excavator_Math {

    public static List<BlockPos> get3x3Blocks(BlockPos center, Direction side) {
        List<BlockPos> blocks = new ArrayList<>();

        // Si le joueur regarde le haut ou le bas d'un bloc (Minage horizontal au sol/plafond)
        if (side == Direction.UP || side == Direction.DOWN) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    blocks.add(center.add(x, 0, z));
                }
            }
        }
        // Si le joueur regarde une face Nord ou Sud (Mur face à lui)
        else if (side == Direction.NORTH || side == Direction.SOUTH) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    blocks.add(center.add(x, y, 0));
                }
            }
        }
        // Si le joueur regarde une face Est ou Ouest (Mur sur les côtés)
        else if (side == Direction.EAST || side == Direction.WEST) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    blocks.add(center.add(0, y, z));
                }
            }
        }

        return blocks;
    }
}