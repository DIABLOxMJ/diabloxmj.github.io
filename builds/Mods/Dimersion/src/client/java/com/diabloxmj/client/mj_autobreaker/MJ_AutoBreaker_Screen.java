package com.diabloxmj.client.mj_autobreaker;

import com.diabloxmj.mj_autobreaker.MJ_AutoBreaker_ScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MJ_AutoBreaker_Screen extends HandledScreen<MJ_AutoBreaker_ScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("dimersion", "textures/gui/container/mj_autobreaker.png");

    public MJ_AutoBreaker_Screen(MJ_AutoBreaker_ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // On applique la signature exacte trouvée dans ton DrawContext :
        // (pipeline, sprite, x, y, u, v, width, height, textureWidth, textureHeight, color)
        context.drawTexture(
                net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, // Le pipeline d'interface
                TEXTURE,                                            // Notre Identifier
                x,                                                  // Position X à l'écran
                y,                                                  // Position Y à l'écran
                0.0F,                                               // U (début horizontal dans le .png)
                0.0F,                                               // V (début vertical dans le .png)
                this.backgroundWidth,                               // Largeur du rectangle à dessiner
                this.backgroundHeight,                              // Hauteur du rectangle à dessiner
                256,                                                // Largeur totale de ton fichier .png
                256,                                                // Hauteur totale de ton fichier .png
                0xFFFFFFFF                                          // Couleur blanche opaque (pas de teinte)
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}