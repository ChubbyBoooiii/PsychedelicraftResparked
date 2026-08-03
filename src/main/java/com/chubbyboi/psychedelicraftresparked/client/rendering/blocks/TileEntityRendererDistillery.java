package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDistillery;
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

public class TileEntityRendererDistillery extends TileEntitySpecialRenderer<TileEntityDistillery> {

    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(Tags.MOD_ID, "models/block/distillery.obj");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/blocks/distillery.png");
    private static final float FLUID_SCALE = 1.0F / 16.0F;

    private static float[] modelData;

    @Override
    public void render(TileEntityDistillery tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.502D, z + 0.5D);
        GlStateManager.rotate(-90.0F * tileEntity.getRotation() + 180.0F, 0.0F, 1.0F, 0.0F);

        GlStateManager.disableCull();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5D, -0.5D, 0.5D);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        bindTexture(TEXTURE);
        renderModel();
        GlStateManager.popMatrix();
        GlStateManager.enableCull();

        FluidStack fluidStack = tileEntity.getTank().getFluid();
        if (fluidStack != null && fluidStack.amount > 0) {
            float fluidHeight = 2.8F * MathHelper.clamp((float) fluidStack.amount / (float) TileEntityDistillery.CAPACITY, 0.0F, 1.0F);
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
        List<float[]> normals = new ArrayList<>();
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
                } else if (line.startsWith("vn ")) {
                    String[] p = line.substring(3).trim().split("\\s+");
                    normals.add(new float[] {Float.parseFloat(p[0]), Float.parseFloat(p[1]), Float.parseFloat(p[2])});
                } else if (line.startsWith("vt ")) {
                    String[] p = line.substring(3).trim().split("\\s+");
                    uvs.add(new float[] {Float.parseFloat(p[0]), Float.parseFloat(p[1])});
                } else if (line.startsWith("f ")) {
                    String[] p = line.substring(2).trim().split("\\s+");
                    for (String token : p) {
                        String[] idx = token.split("/");
                        float[] v = verts.get(Integer.parseInt(idx[0]) - 1);
                        float[] uv = uvs.get(Integer.parseInt(idx[1]) - 1);
                        float[] n = normals.get(Integer.parseInt(idx[2]) - 1);
                        data.add(v[0]); data.add(v[1]); data.add(v[2]);
                        data.add(n[0]); data.add(n[1]); data.add(n[2]);
                        // OBJ v-coordinates are bottom-origin, MC texture v-coordinates are top-origin.
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