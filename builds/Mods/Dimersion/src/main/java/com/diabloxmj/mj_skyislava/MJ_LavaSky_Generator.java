package com.diabloxmj.mj_skyislava;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.tick.OrderedTick;
import java.util.Random;

public class MJ_LavaSky_Generator {

    public static void generateCeiling(StructureWorldAccess world, Chunk chunk) {
        int coucheMaster = 319;
        int coucheRandom = 318;

        Random random = new Random();

        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();

        int chunkX = chunk.getPos().getStartX();
        int chunkZ = chunk.getPos().getStartZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                BlockPos posMaster = new BlockPos(chunkX + x, coucheMaster, chunkZ + z);
                world.setBlockState(posMaster, Blocks.LAVA.getDefaultState(), 3);
                world.getFluidTickScheduler().scheduleTick(new OrderedTick<>(Fluids.LAVA, posMaster, 1L, world.getTickOrder()));

                if (random.nextFloat() < 0.33f) {
                    BlockPos posRandom = new BlockPos(chunkX + x, coucheRandom, chunkZ + z);
                    world.setBlockState(posRandom, Blocks.LAVA.getDefaultState(), 3);
                    world.updateNeighbors(posRandom, Blocks.LAVA);
                    world.getFluidTickScheduler().scheduleTick(new OrderedTick<>(Fluids.LAVA, posRandom, 1L, world.getTickOrder()));

                    int highColonne = random.nextInt(3) + 1;

                    for (int i = 1; i <= highColonne; i++) {
                        int yCible = coucheRandom - i;
                        BlockPos posColonne = new BlockPos(chunkX + x, yCible, chunkZ + z);

                        world.setBlockState(posColonne, Blocks.LAVA.getDefaultState(), 3);

                        world.getFluidTickScheduler().scheduleTick(new OrderedTick<>(Fluids.LAVA, posColonne, 1L, world.getTickOrder()));
                    }
                }
            }
        }
    }
}