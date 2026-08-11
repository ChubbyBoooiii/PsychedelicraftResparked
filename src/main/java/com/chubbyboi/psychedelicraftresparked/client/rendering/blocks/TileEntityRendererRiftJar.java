package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.client.rendering.bezier.BezierPath;
import com.chubbyboi.psychedelicraftresparked.client.rendering.bezier.BezierPathCreator;
import com.chubbyboi.psychedelicraftresparked.client.rendering.bezier.BezierTextRenderer;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.ZeroMatterShader;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityRiftJar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class TileEntityRendererRiftJar extends TileEntitySpecialRenderer<TileEntityRiftJar> {

    private final ModelBase model = new ModelRiftJar();
    private final ResourceLocation texture = new ResourceLocation(Tags.MOD_ID, "textures/blocks/rift_jar.png");
    private final ResourceLocation crackedTexture = new ResourceLocation(Tags.MOD_ID, "textures/blocks/rift_jar_cracked.png");
    private final ResourceLocation[] zeroScreenTextures = new ResourceLocation[8];

    private final BezierPath sphereBezierPath = BezierPathCreator.createSpiraledSphere(3.0, 8.0, 0.2);
    private final BezierPath outgoingBezierPath = BezierPathCreator.createSpiraledBezierPath(0.06, 6.0, 6.0, new double[]{0.0, 1.0, 0.0}, 0.2, 0.0);
    private final BezierTextRenderer bezierTextRenderer = new BezierTextRenderer();

    public TileEntityRendererRiftJar() {
        for (int i = 0; i < zeroScreenTextures.length; i++) {
            zeroScreenTextures[i] = new ResourceLocation(Tags.MOD_ID, "textures/entities/zero_screen_" + i + ".png");
        }
    }

    @Override
    public void render(TileEntityRiftJar tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        float ticks = tileEntity.ticksAliveVisual + partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        Entity fakeEntity = new EntityTippedArrow(tileEntity.getWorld());
        fakeEntity.rotationYaw = tileEntity.fractionOpen;
        fakeEntity.rotationPitch = tileEntity.fractionHandleUp
            * (1.0f + MathHelper.sin(tileEntity.ticksAliveVisual * 0.1f) * 0.1f);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-90.0f * tileEntity.blockRotation + 270.0f, 0.0f, 1.0f, 0.0f);

        if (tileEntity.currentRiftFraction > 0.0f) {
            renderZeroInsides(ticks, Math.min(tileEntity.currentRiftFraction, 1.0f));
        }

        GlStateManager.translate(0.0, 1.0, 0.0);
        GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        bindTexture(texture);
        model.render(fakeEntity, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f, 0.0625f);

        float crackedVisibility = Math.min((tileEntity.currentRiftFraction - 0.5f) * 2.0f, 1.0f);
        if (crackedVisibility > 0.0f) {
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );

            GlStateManager.color(1.0f, 1.0f, 1.0f, crackedVisibility);
            bindTexture(crackedTexture);
            model.render(fakeEntity, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f, 0.0625f);

            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();

        renderBeams(tileEntity, x, y, z, ticks);
    }

    // Interior void shape, in the same units as the jar model's own boxes
    private static final float[][] INSIDE_BOXES = {
        {-4.0f, 0.0f, -4.0f, 8.0f, 5.0f, 8.0f},
        {-3.0f, 5.0f, -3.0f, 6.0f, 2.0f, 6.0f},
        {-4.0f, 7.0f, -4.0f, 8.0f, 5.0f, 8.0f},
    };
    private static final float INSIDE_TOTAL_HEIGHT = 12.0f;

    // Fill glitch texture like a liquid level
    private void renderZeroInsides(float ticks, float fillFraction) {
        int frame = MathHelper.floor(ticks * 0.5f);
        bindTexture(zeroScreenTextures[frame % zeroScreenTextures.length]);

        Random cellRandom = new Random(frame);
        float cellOffsetX = cellRandom.nextInt(10) * 0.1f * 70.0f;
        float cellOffsetY = cellRandom.nextInt(8) * 0.125f * 112.0f;

        ZeroMatterShader shader = ZeroMatterShader.getInstance();
        boolean shaderActive = shader.isAvailable();
        if (shaderActive) {
            shader.activate(cellOffsetX, cellOffsetY);
        }

        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        float fillHeight = fillFraction * INSIDE_TOTAL_HEIGHT;
        float in = 0.001f;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        for (float[] box : INSIDE_BOXES) {
            addFilledCuboid(buffer, box[0] + in, box[1] + in, box[2] + in, box[3] - in * 2, box[4] - in * 2, box[5] - in * 2, fillHeight);
        }
        tessellator.draw();

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();

        if (shaderActive) {
            shader.deactivate();
        }
    }

    private void addFilledCuboid(BufferBuilder buffer, float x, float y, float z, float sizeX, float sizeY, float sizeZ, float fillHeight) {
        float clippedHeight = MathHelper.clamp(fillHeight - y, 0.0f, sizeY);
        if (clippedHeight > 0.0f) {
            addCuboid(buffer, x, y, z, sizeX, clippedHeight, sizeZ, 255);
        }
    }

    private void addCuboid(BufferBuilder buffer, float x, float y, float z, float sizeX, float sizeY, float sizeZ, int alpha) {
        float tM = 1.0f / 16.0f;
        float cx = (x + sizeX * 0.5f) * tM;
        float cy = (y + sizeY * 0.5f) * tM - 0.5f;
        float cz = (z + sizeZ * 0.5f) * tM;
        float hx = sizeX * tM * 0.5f;
        float hy = sizeY * tM * 0.5f;
        float hz = sizeZ * tM * 0.5f;

        vertex(buffer, cx - hx, cy - hy, cz - hz, alpha);
        vertex(buffer, cx - hx, cy + hy, cz - hz, alpha);
        vertex(buffer, cx + hx, cy + hy, cz - hz, alpha);
        vertex(buffer, cx + hx, cy - hy, cz - hz, alpha);

        vertex(buffer, cx - hx, cy - hy, cz + hz, alpha);
        vertex(buffer, cx + hx, cy - hy, cz + hz, alpha);
        vertex(buffer, cx + hx, cy + hy, cz + hz, alpha);
        vertex(buffer, cx - hx, cy + hy, cz + hz, alpha);

        vertex(buffer, cx - hx, cy - hy, cz - hz, alpha);
        vertex(buffer, cx - hx, cy - hy, cz + hz, alpha);
        vertex(buffer, cx - hx, cy + hy, cz + hz, alpha);
        vertex(buffer, cx - hx, cy + hy, cz - hz, alpha);

        vertex(buffer, cx + hx, cy - hy, cz - hz, alpha);
        vertex(buffer, cx + hx, cy + hy, cz - hz, alpha);
        vertex(buffer, cx + hx, cy + hy, cz + hz, alpha);
        vertex(buffer, cx + hx, cy - hy, cz + hz, alpha);

        vertex(buffer, cx - hx, cy + hy, cz - hz, alpha);
        vertex(buffer, cx - hx, cy + hy, cz + hz, alpha);
        vertex(buffer, cx + hx, cy + hy, cz + hz, alpha);
        vertex(buffer, cx + hx, cy + hy, cz - hz, alpha);

        vertex(buffer, cx - hx, cy - hy, cz - hz, alpha);
        vertex(buffer, cx + hx, cy - hy, cz - hz, alpha);
        vertex(buffer, cx + hx, cy - hy, cz + hz, alpha);
        vertex(buffer, cx - hx, cy - hy, cz + hz, alpha);
    }

    private void vertex(BufferBuilder buffer, float x, float y, float z, int alpha) {
        buffer.pos(x, y, z).tex(0.0, 0.0).color(255, 255, 255, alpha).endVertex();
    }

    private void renderBeams(TileEntityRiftJar tileEntity, double x, double y, double z, float ticks) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().standardGalacticFontRenderer;
        if (fontRenderer == null) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.6, z + 0.5);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        for (TileEntityRiftJar.JarRiftConnection connection : tileEntity.riftConnections) {
            if (connection.bezierPath3D == null) {
                connection.bezierPath3D = BezierPathCreator.createSpiraledBezierPath(0.1, 0.5, 8.0,
                    new double[]{
                        connection.entityX - (tileEntity.getPos().getX() + 0.5),
                        connection.entityY - (tileEntity.getPos().getY() + 0.6),
                        connection.entityZ - (tileEntity.getPos().getZ() + 0.5)
                    }, 0.2, 0.0);
            }

            bezierTextRenderer.setText("This is a small spiral.");
            bezierTextRenderer.setSpreadToFill(true);
            bezierTextRenderer.setShift(ticks * -0.002);
            bezierTextRenderer.setCapBottom(0.0);
            bezierTextRenderer.setCapTop(connection.fractionUp);

            bezierTextRenderer.render(connection.bezierPath3D, fontRenderer);

            if (connection.fractionUp > 0.0f) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(
                    connection.entityX - (tileEntity.getPos().getX() + 0.5),
                    connection.entityY - (tileEntity.getPos().getY() + 0.6),
                    connection.entityZ - (tileEntity.getPos().getZ() + 0.5)
                );

                bezierTextRenderer.setText(cheeseString("This is a small circle.", 1.0f - connection.fractionUp, 42));
                bezierTextRenderer.setSpreadToFill(true);
                bezierTextRenderer.setShift(ticks * -0.002);
                bezierTextRenderer.setCapBottom(0.0);
                bezierTextRenderer.setCapTop(1.0);

                bezierTextRenderer.render(sphereBezierPath, fontRenderer);

                GlStateManager.popMatrix();
            }
        }

        float outgoingStrength = tileEntity.fractionHandleUp * tileEntity.fractionOpen;
        if (outgoingStrength > 0.0f) {
            bezierTextRenderer.setText("This is a small spiral.");
            bezierTextRenderer.setSpreadToFill(true);
            bezierTextRenderer.setShift(ticks * 0.002);
            bezierTextRenderer.setCapBottom(0.0);
            bezierTextRenderer.setCapTop(outgoingStrength);

            bezierTextRenderer.render(outgoingBezierPath, fontRenderer);
        }

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private static String cheeseString(String string, float effect, long seed) {
        if (effect <= 0.0f) {
            return string;
        }

        Random rand = new Random(seed);
        StringBuilder builder = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            builder.append(rand.nextFloat() <= effect ? ' ' : string.charAt(i));
        }
        return builder.toString();
    }
}