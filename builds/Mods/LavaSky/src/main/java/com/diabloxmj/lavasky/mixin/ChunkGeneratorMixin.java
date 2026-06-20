package com.diabloxmj.lavasky.mixin;

import com.diabloxmj.lavasky.LavaSkyGenerator;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {

    @Inject(method = "generateFeatures", at = @At("RETURN"))
    private void onGenerateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor, CallbackInfo ci) {
        if (world.toServerWorld().getRegistryKey().getValue().getPath().equals("overworld")) {
            if (com.diabloxmj.lavasky.LavaSkyConfig.get().ActiveLavaSky) {
                LavaSkyGenerator.generateCeiling(world, chunk);
            }
        }
    }
}