package com.diabloxmj.mj_skyislava.mixin;

import com.diabloxmj.mj_skyislava.MJ_LavaSky_Generator;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class MJ_LavaSky_Mixin_Chunk {

    @Inject(method = "generateFeatures", at = @At("RETURN"))
    private void onGenerateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor, CallbackInfo ci) {
        if (world.toServerWorld().getRegistryKey().getValue().getPath().equals("overworld")) {
            if (com.diabloxmj.mj_skyislava.MJ_LavaSky_Config.get().ActiveLavaSky) {
                MJ_LavaSky_Generator.generateCeiling(world, chunk);
            }
        }
    }
}