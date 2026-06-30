package com.diabloxmj.mj_excavator;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import java.util.List;

public class MJ_Pickavator_Item extends Item implements MJ_Excavator_Item {

    public MJ_Pickavator_Item(Settings settings) {
        super(settings);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient() && miner instanceof ServerPlayerEntity player) {

            // Raycast pour détecter la face du bloc visée
            HitResult hitResult = player.raycast(4.5, 0.0F, false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                Direction side = blockHitResult.getSide();

                // On récupère notre zone 3x3
                List<BlockPos> targetBlocks = MJ_Excavator_Math.get3x3Blocks(pos, side);

                for (BlockPos targetPos : targetBlocks) {
                    if (targetPos.equals(pos)) continue; // On ignore le bloc central déjà cassé

                    BlockState targetState = world.getBlockState(targetPos);

                    // Utilisation du composant de ton code pour vérifier si l'outil peut détruire ce bloc
                    ToolComponent toolComponent = stack.get(DataComponentTypes.TOOL);
                    if (toolComponent != null && toolComponent.getSpeed(targetState) > 1.0F) {
                        player.interactionManager.tryBreakBlock(targetPos);
                    }
                }
            }
        }

        return super.postMine(stack, world, state, pos, miner);
    }
}