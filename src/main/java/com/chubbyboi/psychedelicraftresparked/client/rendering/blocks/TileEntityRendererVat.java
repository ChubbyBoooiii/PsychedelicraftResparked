package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.block.BlockVat;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityRendererVat extends TileEntitySpecialRenderer<TileEntityVat> {

    @Override
    public void render(TileEntityVat tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.rotate(TileEntityVat.rotationFor(tileEntity.getPrimaryDirection()), 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5D, 0.0D, -0.5D);

        if (!tileEntity.getSolidContents().isEmpty()) {
            renderSolidContents(tileEntity);
        } else {
            renderIngredients(tileEntity, partialTicks);
            renderFluid(tileEntity);
        }

        GlStateManager.popMatrix();
    }

    private void renderFluid(TileEntityVat tileEntity) {
        FluidStack fluid = tileEntity.getTank().getFluid();
        if (fluid == null || fluid.amount <= 0) {
            return;
        }

        float fillFraction = Math.min(1.0F, (float) fluid.amount / TileEntityVat.CAPACITY);
        float topY = BlockVat.BASIN_FLOOR_Y + fillFraction * (BlockVat.BASIN_RIM_Y - BlockVat.BASIN_FLOOR_Y);

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

    private static final float[][] INGREDIENT_SLOT_POSITIONS = {
        {1.550F, 1.000F},
        {1.275F, 1.476F},
        {0.725F, 1.476F},
        {0.450F, 1.000F},
        {0.725F, 0.524F},
        {1.275F, 0.524F},
        {1.000F, 1.000F},
    };

    private void renderIngredients(TileEntityVat tileEntity, float partialTicks) {
        FluidStack fluid = tileEntity.getTank().getFluid();
        boolean hasFluid = fluid != null && fluid.amount > 0;
        float fillFraction = hasFluid ? Math.min(1.0F, (float) fluid.amount / TileEntityVat.CAPACITY) : 0.0F;
        float topY = BlockVat.BASIN_FLOOR_Y + fillFraction * (BlockVat.BASIN_RIM_Y - BlockVat.BASIN_FLOOR_Y);

        float ticks = tileEntity.getWorld().getTotalWorldTime() + partialTicks;

        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.enableLighting();
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        for (int i = 0; i < TileEntityVat.INGREDIENT_SLOTS; i++) {
            ItemStack stack = tileEntity.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            float slotX = INGREDIENT_SLOT_POSITIONS[i][0];
            float slotZ = INGREDIENT_SLOT_POSITIONS[i][1];
            float phase = i * 137.0F;

            for (int c = 0; c < stack.getCount(); c++) {
                double angle = c * 2.4;
                float posX = slotX + (float) (Math.cos(angle) * 0.06 * c);
                float posZ = slotZ + (float) (Math.sin(angle) * 0.06 * c);
                float baseRotation = (i * 47 + c * 91) % 360;

                GlStateManager.pushMatrix();
                GlStateManager.translate(posX, 0.0D, posZ);

                if (hasFluid) {
                    float wave = MathHelper.sin((ticks + phase) / 8.0F);
                    float bob = -0.03F + (wave - 1.0F) * 0.025F;
                    GlStateManager.translate(0.0F, topY + bob, 0.0F);
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate((ticks + baseRotation) % 360.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.scale(0.35F, 0.35F, 0.35F);
                } else {
                    // Not enough liquid to float in yet - just lie flat on the basin floor.
                    GlStateManager.translate(0.0F, BlockVat.BASIN_FLOOR_Y + 0.001F, 0.0F);
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(baseRotation, 0.0F, 0.0F, 1.0F);
                    GlStateManager.scale(0.4F, 0.4F, 0.4F);
                }

                mc.getRenderItem().renderItem(stack, mc.getRenderItem().getItemModelWithOverrides(stack, null, null));

                GlStateManager.popMatrix();
            }
        }

        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSolidContents(TileEntityVat tileEntity) {
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

        drawTopQuad(sprite, BlockVat.BASIN_RIM_Y);
    }

    private void drawTopQuad(TextureAtlasSprite sprite, float topY) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(BlockVat.BASIN_MIN_XZ, topY, BlockVat.BASIN_MIN_XZ).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        buffer.pos(BlockVat.BASIN_MIN_XZ, topY, BlockVat.BASIN_MAX_XZ).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        buffer.pos(BlockVat.BASIN_MAX_XZ, topY, BlockVat.BASIN_MAX_XZ).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        buffer.pos(BlockVat.BASIN_MAX_XZ, topY, BlockVat.BASIN_MIN_XZ).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        tessellator.draw();
    }
}