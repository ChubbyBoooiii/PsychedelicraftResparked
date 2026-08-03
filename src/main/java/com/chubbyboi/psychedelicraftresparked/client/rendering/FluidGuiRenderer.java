package com.chubbyboi.psychedelicraftresparked.client.rendering;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.FluidStack;

public class FluidGuiRenderer {
    private static final int TILE_SIZE = 32;

    private FluidGuiRenderer() {
    }

    public static void drawTiledFluidRect(FluidStack fluid, int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getStill(fluid).toString());

        int color = FluidHelper.getWorldRenderColor(fluid);
        float a = ((color >> 24) & 0xFF) / 255.0F;
        if (a <= 0.0F) {
            a = 1.0F;
        }
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(r, g, b, a);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int dx = 0; dx < width; dx += TILE_SIZE) {
            int drawWidth = Math.min(TILE_SIZE, width - dx);
            float u1 = sprite.getInterpolatedU(0.0);
            float u2 = sprite.getInterpolatedU(drawWidth * 16.0 / TILE_SIZE);
            for (int dy = 0; dy < height; dy += TILE_SIZE) {
                int drawHeight = Math.min(TILE_SIZE, height - dy);
                float v1 = sprite.getInterpolatedV(0.0);
                float v2 = sprite.getInterpolatedV(drawHeight * 16.0 / TILE_SIZE);

                int x1 = x + dx, x2 = x1 + drawWidth;
                int y1 = y + dy, y2 = y1 + drawHeight;

                buffer.pos(x1, y2, 0).tex(u1, v2).endVertex();
                buffer.pos(x2, y2, 0).tex(u2, v2).endVertex();
                buffer.pos(x2, y1, 0).tex(u2, v1).endVertex();
                buffer.pos(x1, y1, 0).tex(u1, v1).endVertex();
            }
        }
        tessellator.draw();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
    }
}