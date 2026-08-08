package com.chubbyboi.psychedelicraftresparked.client.rendering;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ZeroMatterShader;
import com.chubbyboi.psychedelicraftresparked.entities.EntityRealityRift;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Random;

public class RenderRealityRift extends Render<EntityRealityRift> {

    private static final int RAY_COUNT = 20;

    private final ResourceLocation[] zeroScreenTextures = new ResourceLocation[8];
    private final ResourceLocation zeroCenterTexture = new ResourceLocation(Tags.MOD_ID, "textures/entities/zero_centre.png");

    public RenderRealityRift(RenderManager renderManager) {
        super(renderManager);
        for (int i = 0; i < zeroScreenTextures.length; i++) {
            zeroScreenTextures[i] = new ResourceLocation(Tags.MOD_ID, "textures/entities/zero_screen_" + i + ".png");
        }
    }

    @Override
    public void doRender(EntityRealityRift entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height * 0.5D, z);

        float riftSize = entity.visualRiftSize;
        float scale = riftSize < 0.01F ? riftSize * 10.0F : 0.1F + (riftSize - 0.01F) * 0.1F;
        if (scale <= 0.0F) {
            GlStateManager.popMatrix();
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.disableLighting();

        float critStatus = entity.getCriticalStatus();
        float ticks = entity.ticksExisted + partialTicks + critStatus * critStatus * 3000.0F;
        renderLightRays(ticks);

        GlStateManager.scale(5.0, 5.0, 5.0);
        renderCenterGlow(partialTicks);

        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    private void renderLightRays(float ticks) {
        int frame = MathHelper.floor(ticks * 0.5F);
        bindTexture(zeroScreenTextures[frame % zeroScreenTextures.length]);

        Random cellRandom = new Random(frame);
        float cellOffsetX = cellRandom.nextInt(10) * 0.1F * 70.0F;
        float cellOffsetY = cellRandom.nextInt(8) * 0.125F * 112.0F;

        ZeroMatterShader shader = ZeroMatterShader.getInstance();
        boolean shaderActive = shader.isAvailable();
        if (shaderActive) {
            shader.activate(cellOffsetX, cellOffsetY);
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        Random rayRandom = new Random(432L);
        for (int i = 0; i < RAY_COUNT; i++) {
            float xLogFunc = ((i / (float) RAY_COUNT * 28493.0F + ticks) / 10.0F) % 20.0F;
            if (xLogFunc > 10.0F) {
                xLogFunc = 20.0F - xLogFunc;
            }
            float yLogFunc = 1.0F / (1.0F + (float) Math.pow(2.71828F, -0.8F * xLogFunc) * 99.0F);
            float lightAlpha = yLogFunc;

            if (lightAlpha <= 0.01F) {
                continue;
            }

            GlStateManager.rotate(rayRandom.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(rayRandom.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(rayRandom.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(rayRandom.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(rayRandom.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(rayRandom.nextFloat() * 360.0F + ticks / 200.0F * 90.0F, 0.0F, 0.0F, 1.0F);

            float width = 2.5F;
            float height = rayRandom.nextFloat() * 20.0F + 5.0F;
            float spread = rayRandom.nextFloat() * 2.0F + 1.0F;
            int alpha = (int) (255.0F * lightAlpha);

            buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(255, 255, 255, alpha).endVertex();
            buffer.pos(-width * spread, height, -0.5F * spread).tex(0.0, 0.0).color(255, 255, 255, 0).endVertex();
            buffer.pos(width * spread, height, -0.5F * spread).tex(0.0, 0.0).color(255, 255, 255, 0).endVertex();
            buffer.pos(0.0, height, 1.0F * spread).tex(0.0, 0.0).color(255, 255, 255, 0).endVertex();
            buffer.pos(-width * spread, height, -0.5F * spread).tex(0.0, 0.0).color(255, 255, 255, 0).endVertex();
            tessellator.draw();
        }

        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableAlpha();

        if (shaderActive) {
            shader.deactivate();
        }
    }

    private void renderCenterGlow(float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        bindTexture(zeroCenterTexture);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        float halfSize = 1.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(-halfSize, -halfSize, 0.0).tex(0.0, 1.0).color(255, 255, 255, 255).endVertex();
        buffer.pos(halfSize, -halfSize, 0.0).tex(1.0, 1.0).color(255, 255, 255, 255).endVertex();
        buffer.pos(halfSize, halfSize, 0.0).tex(1.0, 0.0).color(255, 255, 255, 255).endVertex();
        buffer.pos(-halfSize, halfSize, 0.0).tex(0.0, 0.0).color(255, 255, 255, 255).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();

        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityRealityRift entity) {
        return null;
    }
}