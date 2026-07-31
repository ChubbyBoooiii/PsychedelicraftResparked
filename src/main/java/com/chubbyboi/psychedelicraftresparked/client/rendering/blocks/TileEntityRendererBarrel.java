package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBarrel;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

public class TileEntityRendererBarrel extends TileEntitySpecialRenderer<TileEntityBarrel> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/blocks/barrel.png");

    private final ModelBarrel model = new ModelBarrel();

    @Override
    public void render(TileEntityBarrel tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        GlStateManager.rotate(-90.0F * tileEntity.getRotation() + 180.0F, 0.0F, 1.0F, 0.0F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 1.0D, 0.0D);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);

        bindTexture(TEXTURE);
        model.render(0.0625F, tileEntity.getTapRotation());

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }
}