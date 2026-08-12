package com.diabloxmj.mj_excavator;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.ArrayList;
import java.util.List;

public class MJ_Mining_Helper {

    public static List<BlockPos> get3x3Blocks(BlockPos targetPos, PlayerEntity player) {
        List<BlockPos> blocks = new ArrayList<>();

        // Si le joueur est accroupi (Shift), on désactive le 3x3 pour lui permettre de miner un seul bloc !
        if (player.isSneaking()) {
            return blocks;
        }

        // On effectue un raycast pour savoir quelle face du bloc le joueur regarde
        HitResult hit = player.raycast(4.5D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return blocks;
        }

        Direction side = ((BlockHitResult) hit).getSide();

        // Selon la face regardée, on calcule l'étalement du 3x3
        int xMin = -1, xMax = 1, yMin = -1, yMax = 1, zMin = -1, zMax = 1;

        if (side == Direction.UP || side == Direction.DOWN) {
            yMin = 0; yMax = 0; // Face haute/basse : on s'étale sur X et Z
        } else if (side == Direction.EAST || side == Direction.WEST) {
            xMin = 0; xMax = 0; // Face Est/Ouest : on s'étale sur Y et Z
        } else if (side == Direction.NORTH || side == Direction.SOUTH) {
            zMin = 0; zMax = 0; // Face Nord/Sud : on s'étale sur X et Y
        }

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    if (x != 0 || y != 0 || z != 0) { // On exclut le bloc central (déjà géré par Minecraft)
                        blocks.add(targetPos.add(x, y, z));
                    }
                }
            }
        }
        return blocks;
    }
}