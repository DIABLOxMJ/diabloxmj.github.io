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
import net.minecraft.util.math.Direction;
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

        BlockHitResult blockHit = (BlockHitResult) client.crosshairTarget;
        BlockPos origin = blockHit.getBlockPos();

        // On récupère la face visée côté client de la même manière
        Direction side = blockHit.getSide();

        // On initialise les coordonnées sur le bloc central
        int minX = origin.getX(), minY = origin.getY(), minZ = origin.getZ();
        int maxX = origin.getX() + 1, maxY = origin.getY() + 1, maxZ = origin.getZ() + 1;

        if (!player.isSneaking()) {
            // On force l'extension à un 3x3 parfait selon la face, sans vérifier le contenu des blocs
            if (side == Direction.UP || side == Direction.DOWN) {
                minX -= 1; maxX += 1;
                minZ -= 1; maxZ += 1;
            } else if (side == Direction.NORTH || side == Direction.SOUTH) {
                minX -= 1; maxX += 1;
                minY -= 1; maxY += 1;
            } else if (side == Direction.EAST || side == Direction.WEST) {
                minZ -= 1; maxZ += 1;
                minY -= 1; maxY += 1;
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