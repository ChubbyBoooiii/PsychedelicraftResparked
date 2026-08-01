package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityFlask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TileEntityRendererFlask extends TileEntitySpecialRenderer<TileEntityFlask> {

    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(Tags.MOD_ID, "models/block/flask.obj");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/blocks/flask.png");
    private static final float FLUID_SCALE = 1.0F / 16.0F;

    private static float[] modelData;

    @Override
    public void render(TileEntityFlask tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.502D, z + 0.5D);

        GlStateManager.disableCull();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, -0.5D, 0.0D);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        bindTexture(TEXTURE);
        renderModel();
        GlStateManager.popMatrix();
        GlStateManager.enableCull();

        FluidStack fluidStack = tileEntity.getTank().getFluid();
        if (fluidStack != null && fluidStack.amount > 0) {
            float fluidHeight = 2.8F * MathHelper.clamp((float) fluidStack.amount / (float) TileEntityFlask.CAPACITY, 0.0F, 1.0F);
            renderFluidBox(fluidStack, fluidHeight);
        }

        GlStateManager.popMatrix();
    }

    private static void renderModel() {
        if (modelData == null) {
            modelData = loadModel();
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (int i = 0; i < modelData.length; i += 8) {
            buffer.pos(modelData[i], modelData[i + 1], modelData[i + 2])
                .tex(modelData[i + 6], modelData[i + 7])
                .normal(modelData[i + 3], modelData[i + 4], modelData[i + 5])
                .endVertex();
        }
        tessellator.draw();
    }

    private static float[] loadModel() {
        List<float[]> verts = new ArrayList<>();
        List<float[]> uvs = new ArrayList<>();
        List<Float> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Minecraft.getMinecraft().getResourceManager().getResource(MODEL_LOCATION).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("v ")) {
                    String[] p = line.substring(2).trim().split("\\s+");
                    verts.add(new float[] {Float.parseFloat(p[0]), Float.parseFloat(p[1]), Float.parseFloat(p[2])});
                } else if (line.startsWith("vt ")) {
                    String[] p = line.substring(3).trim().split("\\s+");
                    uvs.add(new float[] {Float.parseFloat(p[0]), Float.parseFloat(p[1])});
                } else if (line.startsWith("f ")) {
                    String[] p = line.substring(2).trim().split("\\s+");
                    float[][] faceVerts = new float[p.length][];
                    float[][] faceUvs = new float[p.length][];
                    for (int i = 0; i < p.length; i++) {
                        String[] idx = p[i].split("/");
                        faceVerts[i] = verts.get(Integer.parseInt(idx[0]) - 1);
                        faceUvs[i] = uvs.get(Integer.parseInt(idx[1]) - 1);
                    }
                    float[] normal = faceNormal(faceVerts[0], faceVerts[1], faceVerts[2]);
                    for (int i = 0; i < p.length; i++) {
                        float[] v = faceVerts[i];
                        float[] uv = faceUvs[i];
                        data.add(v[0]); data.add(v[1]); data.add(v[2]);
                        data.add(normal[0]); data.add(normal[1]); data.add(normal[2]);

                        data.add(uv[0]); data.add(1.0F - uv[1]);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + MODEL_LOCATION, e);
        }

        float[] result = new float[data.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = data.get(i);
        }
        return result;
    }

    private static float[] faceNormal(float[] a, float[] b, float[] c) {
        float ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2];
        float vx = c[0] - a[0], vy = c[1] - a[1], vz = c[2] - a[2];
        float nx = uy * vz - uz * vy;
        float ny = uz * vx - ux * vz;
        float nz = ux * vy - uy * vx;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0.0F) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        return new float[] {nx, ny, nz};
    }

    private static void renderFluidBox(FluidStack fluid, float fluidHeight) {
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getStill(fluid).toString());

        int color = FluidHelper.getWorldRenderColor(fluid);
        float a = ((color >> 24) & 0xFF) / 255.0F;
        if (a <= 0.0F) {
            a = 1.0F;
        }
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(r, g, b, a);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);

        renderFluidQuad(buffer, sprite, -1.9F, -8.0F, -3.9F, 3.8F, fluidHeight, 0.9F, EnumFacing.NORTH, EnumFacing.UP);
        renderFluidQuad(buffer, sprite, -1.9F, -8.0F, 3.0F, 3.8F, fluidHeight, 0.9F, EnumFacing.SOUTH, EnumFacing.UP);
        renderFluidQuad(buffer, sprite, -3.9F, -8.0F, -1.9F, 0.9F, fluidHeight, 3.8F, EnumFacing.WEST, EnumFacing.UP);
        renderFluidQuad(buffer, sprite, 3.0F, -8.0F, -1.9F, 0.9F, fluidHeight, 3.8F, EnumFacing.EAST, EnumFacing.UP);
        renderFluidQuad(buffer, sprite, -3.0F, -8.0F, -3.0F, 6.0F, fluidHeight, 6.0F, EnumFacing.UP);

        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
    }

    private static void renderFluidQuad(BufferBuilder buffer, TextureAtlasSprite sprite, float px, float py, float pz, float width, float height, float length, EnumFacing... directions) {
        float x = px * FLUID_SCALE, y = py * FLUID_SCALE, z = pz * FLUID_SCALE;
        float w = width * FLUID_SCALE, h = height * FLUID_SCALE, l = length * FLUID_SCALE;
        float u0 = sprite.getMinU(), u1 = sprite.getMaxU(), v0 = sprite.getMinV(), v1 = sprite.getMaxV();

        for (EnumFacing direction : directions) {
            switch (direction) {
                case DOWN:
                    buffer.pos(x, y, z).tex(u0, v0).endVertex();
                    buffer.pos(x + w, y, z).tex(u1, v0).endVertex();
                    buffer.pos(x + w, y, z + l).tex(u1, v1).endVertex();
                    buffer.pos(x, y, z + l).tex(u0, v1).endVertex();
                    break;
                case UP:
                    buffer.pos(x, y + h, z).tex(u0, v0).endVertex();
                    buffer.pos(x, y + h, z + l).tex(u0, v1).endVertex();
                    buffer.pos(x + w, y + h, z + l).tex(u1, v1).endVertex();
                    buffer.pos(x + w, y + h, z).tex(u1, v0).endVertex();
                    break;
                case EAST:
                    buffer.pos(x + w, y, z).tex(u0, v0).endVertex();
                    buffer.pos(x + w, y + h, z).tex(u1, v0).endVertex();
                    buffer.pos(x + w, y + h, z + l).tex(u1, v1).endVertex();
                    buffer.pos(x + w, y, z + l).tex(u0, v1).endVertex();
                    break;
                case WEST:
                    buffer.pos(x, y, z).tex(u0, v0).endVertex();
                    buffer.pos(x, y, z + l).tex(u1, v0).endVertex();
                    buffer.pos(x, y + h, z + l).tex(u1, v1).endVertex();
                    buffer.pos(x, y + h, z).tex(u0, v1).endVertex();
                    break;
                case NORTH:
                    buffer.pos(x, y, z).tex(u0, v0).endVertex();
                    buffer.pos(x, y + h, z).tex(u0, v1).endVertex();
                    buffer.pos(x + w, y + h, z).tex(u1, v1).endVertex();
                    buffer.pos(x + w, y, z).tex(u1, v0).endVertex();
                    break;
                case SOUTH:
                    buffer.pos(x, y, z + l).tex(u0, v0).endVertex();
                    buffer.pos(x + w, y, z + l).tex(u1, v0).endVertex();
                    buffer.pos(x + w, y + h, z + l).tex(u1, v1).endVertex();
                    buffer.pos(x, y + h, z + l).tex(u0, v1).endVertex();
                    break;
            }
        }
    }
}