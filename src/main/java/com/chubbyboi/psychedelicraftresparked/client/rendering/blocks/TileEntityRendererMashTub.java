package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.block.BlockMashTub;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityMashTub;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityRendererMashTub extends TileEntitySpecialRenderer<TileEntityMashTub> {

    @Override
    public void render(TileEntityMashTub tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.rotate(TileEntityMashTub.rotationFor(tileEntity.getPrimaryDirection()), 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5D, 0.0D, -0.5D);

        if (!tileEntity.getSolidContents().isEmpty()) {
            renderSolidContents(tileEntity);
        } else {
            renderFluid(tileEntity);
        }

        GlStateManager.popMatrix();
    }

    private void renderFluid(TileEntityMashTub tileEntity) {
        FluidStack fluid = tileEntity.getTank().getFluid();
        if (fluid == null || fluid.amount <= 0) {
            return;
        }

        float fillFraction = Math.min(1.0F, (float) fluid.amount / TileEntityMashTub.CAPACITY);
        float topY = BlockMashTub.BASIN_FLOOR_Y + fillFraction * (BlockMashTub.BASIN_RIM_Y - BlockMashTub.BASIN_FLOOR_Y);

        ResourceLocation stillLocation = fluid.getFluid().getStill(fluid);
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(stillLocation.toString());

        int color = FluidHelper.getWorldRenderColor(fluid);
        float a = ((color >> 24) & 0xFF) / 255.0F;

        // The texture is already transparent, leave it full alpha
        if (a <= 0.0F) {
            a = 1.0F;
        }
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(r, g, b, a);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        drawTopQuad(sprite, topY);

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
    }

    private void renderSolidContents(TileEntityMashTub tileEntity) {
        ItemStack solid = tileEntity.getSolidContents();
        Block block = Block.getBlockFromItem(solid.getItem());
        if (block == Blocks.AIR) {
            return;
        }

        IBlockState state = block.getStateFromMeta(solid.getMetadata());
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();

        drawTopQuad(sprite, BlockMashTub.BASIN_RIM_Y);
    }

    private void drawTopQuad(TextureAtlasSprite sprite, float topY) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(BlockMashTub.BASIN_MIN_XZ, topY, BlockMashTub.BASIN_MIN_XZ).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        buffer.pos(BlockMashTub.BASIN_MIN_XZ, topY, BlockMashTub.BASIN_MAX_XZ).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        buffer.pos(BlockMashTub.BASIN_MAX_XZ, topY, BlockMashTub.BASIN_MAX_XZ).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        buffer.pos(BlockMashTub.BASIN_MAX_XZ, topY, BlockMashTub.BASIN_MIN_XZ).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        tessellator.draw();
    }
}