package com.diabloxmj.client.mj_excavator;

import com.diabloxmj.mj_excavator.MJ_Excavator_Item;
import com.diabloxmj.mj_excavator.MJ_Mining_Helper;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class MJ_Excavator_Renderer {

    public static void register() {
        WorldRenderEvents.END_MAIN.register(MJ_Excavator_Renderer::renderOverlay);
    }

    private static void renderOverlay(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        PlayerEntity player = client.player;
        ItemStack handStack = player.getMainHandStack();

        if (!(handStack.getItem() instanceof MJ_Excavator_Item)) return;
        // Utilisation du raycast temps réel mis à jour à chaque frame pour éviter la latence
        HitResult hit = client.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos origin = blockHit.getBlockPos();

        int minX = origin.getX(), minY = origin.getY(), minZ = origin.getZ();
        int maxX = origin.getX() + 1, maxY = origin.getY() + 1, maxZ = origin.getZ() + 1;

        if (!player.isSneaking()) {
            for (BlockPos pos : MJ_Mining_Helper.get3x3Blocks(origin, player)) {
                BlockState state = client.world.getBlockState(pos);
                if (handStack.isSuitableFor(state)) {
                    minX = Math.min(minX, pos.getX());
                    minY = Math.min(minY, pos.getY());
                    minZ = Math.min(minZ, pos.getZ());
                    maxX = Math.max(maxX, pos.getX() + 1);
                    maxY = Math.max(maxY, pos.getY() + 1);
                    maxZ = Math.max(maxZ, pos.getZ() + 1);
                }
            }
        }

        if (client.gameRenderer == null || client.gameRenderer.getCamera() == null) return;

        // Position de la caméra (yeux du joueur)
        Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();

        MatrixStack matrices = new MatrixStack();
        VertexConsumer buffer = context.consumers().getBuffer(RenderLayers.lines());

        // 1. On crée une forme cubique brute (VoxelShape) aux dimensions de notre min/max
        VoxelShape boxShape = VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ);

        // 2. On calcule le décalage (offsetX, offsetY, offsetZ) par rapport à la caméra
        // On rajoute un infime recul (-0.005 / +0.005) pour éviter le z-fighting avec les blocs
        double offsetX = -cameraPos.x;
        double offsetY = -cameraPos.y;
        double offsetZ = -cameraPos.z;

        // 3. Couleur ARGB au format entier (Int Hexadécimal) attendu par le shader :
        // Blanc avec une opacité d'environ 100% (0xFFFFFFFF)
        int colorARGB = 0xFFFFFFFF;

        // LA SOLUTION FINALE : On appelle la méthode exacte de ton fichier !
        // Arguments : matrices, vertexConsumers, shape, offsetX, offsetY, offsetZ, color, lineWidth
        VertexRendering.drawOutline(matrices, buffer, boxShape, offsetX, offsetY, offsetZ, colorARGB, 4.0F);
    }
}