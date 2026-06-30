package com.diabloxmj.client.mj_excavator;

import com.diabloxmj.mj_excavator.MJ_Excavator_Item;
import com.diabloxmj.mj_excavator.MJ_Mining_Helper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class MJ_Excavator_Renderer {

    public static void register() {
        WorldRenderEvents.END.register(MJ_Excavator_Renderer::renderOverlay);
    }

    private static void renderOverlay(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        PlayerEntity player = client.player;
        ItemStack handStack = player.getMainHandStack();

        if (!(handStack.getItem() instanceof MJ_Excavator_Item)) return;
        if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) client.crosshairTarget;
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

        Vec3d cameraPos = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();

        matrices.push();
        // Légère surélévation globale pour éviter les clignotements avec le bloc (z-fighting)
        matrices.translate(
                (float)(minX - cameraPos.x) - 0.005f,
                (float)(minY - cameraPos.y) - 0.005f,
                (float)(minZ - cameraPos.z) - 0.005f
        );

        VertexConsumer buffer = context.consumers().getBuffer(net.minecraft.client.render.RenderLayer.getLines());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float dx = (maxX - minX) + 0.01f;
        float dy = (maxY - minY) + 0.01f;
        float dz = (maxZ - minZ) + 0.01f;

        // --- DESSIN DU CADRAGE EN LIGNES PURES (Version 1.21.11 Yarn) ---

        // Base basse
        drawBoxLine(buffer, matrix, 0, 0, 0, dx, 0, 0);
        drawBoxLine(buffer, matrix, dx, 0, 0, dx, 0, dz);
        drawBoxLine(buffer, matrix, dx, 0, dz, 0, 0, dz);
        drawBoxLine(buffer, matrix, 0, 0, dz, 0, 0, 0);

        // Base haute
        drawBoxLine(buffer, matrix, 0, dy, 0, dx, dy, 0);
        drawBoxLine(buffer, matrix, dx, dy, 0, dx, dy, dz);
        drawBoxLine(buffer, matrix, dx, dy, dz, 0, dy, dz);
        drawBoxLine(buffer, matrix, 0, dy, dz, 0, dy, 0);

        // Piliers verticaux
        drawBoxLine(buffer, matrix, 0, 0, 0, 0, dy, 0);
        drawBoxLine(buffer, matrix, dx, 0, 0, dx, dy, 0);
        drawBoxLine(buffer, matrix, dx, 0, dz, dx, dy, dz);
        drawBoxLine(buffer, matrix, 0, 0, dz, 0, dy, dz);

        matrices.pop();
    }

    // Méthode utilitaire corrigée pour la version 1.21.11
    private static void drawBoxLine(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2) {
        // Premier point de la ligne : on applique la matrice directement sur les coordonnées
        buffer.vertex(matrix, x1, y1, z1)
                .color(1.0f, 0.0f, 0.0f, 0.4f)
                .normal(0.0f, 1.0f, 0.0f)
                .endVertex();

        // Deuxième point de la ligne
        buffer.vertex(matrix, x2, y2, z2)
                .color(1.0f, 0.0f, 0.0f, 0.4f)
                .normal(0.0f, 1.0f, 0.0f)
                .endVertex();
    }
}