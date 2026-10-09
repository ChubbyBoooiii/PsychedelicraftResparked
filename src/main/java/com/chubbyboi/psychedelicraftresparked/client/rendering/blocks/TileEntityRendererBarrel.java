package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.block.BlockBarrel;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBarrel;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TileEntityRendererBarrel extends TileEntitySpecialRenderer<TileEntityBarrel> {

    private static final ResourceLocation BODY = new ResourceLocation(Tags.MOD_ID, "block/barrel_body");
    private static final ResourceLocation TAP = new ResourceLocation(Tags.MOD_ID, "block/barrel_tap");
    private static final ResourceLocation TAP_HANDLE = new ResourceLocation(Tags.MOD_ID, "block/barrel_tap_handle");

    private static final float HANDLE_PIVOT_X = 8.0F / 16.0F;
    private static final float HANDLE_PIVOT_Y = 7.0F / 16.0F;
    private static final float HANDLE_PIVOT_Z = -1.0F / 16.0F;

    private static final class WoodModels {
        private List<BakedQuad> body = Collections.emptyList();
        private List<BakedQuad> tap = Collections.emptyList();
        private List<BakedQuad> tapHandle = Collections.emptyList();
    }

    private static final Map<BlockPlanks.EnumType, WoodModels> MODELS = new EnumMap<>(BlockPlanks.EnumType.class);

    private static String getTexture(BlockPlanks.EnumType wood) {
        return Tags.MOD_ID + ":blocks/barrel_" + wood.getName();
    }

    public static void registerTextures(TextureMap map) {
        for (BlockPlanks.EnumType wood : BlockPlanks.EnumType.values()) {
            map.registerSprite(new ResourceLocation(getTexture(wood)));
        }
    }

    public static void bakeModels() {
        MODELS.clear();
        for (BlockPlanks.EnumType wood : BlockPlanks.EnumType.values()) {
            ImmutableMap<String, String> textures = ImmutableMap.of("0", getTexture(wood), "particle", getTexture(wood));
            WoodModels models = new WoodModels();
            models.body = bake(BODY, textures);
            models.tap = bake(TAP, textures);
            models.tapHandle = bake(TAP_HANDLE, textures);
            MODELS.put(wood, models);
        }
    }

    private static List<BakedQuad> bake(ResourceLocation location, ImmutableMap<String, String> textures) {
        try {
            IModel model = ModelLoaderRegistry.getModel(location).retexture(textures);
            IBakedModel baked = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
            List<BakedQuad> quads = new ArrayList<>();
            for (EnumFacing side : EnumFacing.values()) {
                quads.addAll(baked.getQuads(null, side, 0L));
            }
            quads.addAll(baked.getQuads(null, null, 0L));
            return quads;
        } catch (Exception e) {
            PsychedelicraftResparked.LOGGER.error("Failed to load barrel model {}", location, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void render(TileEntityBarrel tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.rotate(-90.0F * tileEntity.getRotation(), 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5D, 0.0D, -0.5D);
        render(getWood(tileEntity), tileEntity.hasTap(), tileEntity.getTapRotation());
        GlStateManager.popMatrix();
    }

    public static void render(BlockPlanks.EnumType wood, boolean hasTap, float tapRotation) {
        WoodModels models = MODELS.get(wood);
        if (models == null) {
            return;
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        renderQuads(models.body);
        if (hasTap) {
            renderQuads(models.tap);
            // Model y/x are flipped relative to the 1.7.10 ModelBase, so its rotation about Y turns the other way here
            GlStateManager.pushMatrix();
            GlStateManager.translate(HANDLE_PIVOT_X, HANDLE_PIVOT_Y, HANDLE_PIVOT_Z);
            GlStateManager.rotate((float) -Math.toDegrees(tapRotation), 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-HANDLE_PIVOT_X, -HANDLE_PIVOT_Y, -HANDLE_PIVOT_Z);
            renderQuads(models.tapHandle);
            GlStateManager.popMatrix();
        }
    }

    private static void renderQuads(List<BakedQuad> quads) {
        if (quads.isEmpty()) {
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        for (BakedQuad quad : quads) {
            LightUtil.renderQuadColor(buffer, quad, 0xFFFFFFFF);
        }
        tessellator.draw();
    }

    private static BlockPlanks.EnumType getWood(TileEntityBarrel tileEntity) {
        IBlockState state = tileEntity.getWorld().getBlockState(tileEntity.getPos());
        return state.getPropertyKeys().contains(BlockBarrel.WOOD_TYPE)
            ? state.getValue(BlockBarrel.WOOD_TYPE)
            : BlockPlanks.EnumType.OAK;
    }
}