package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityRiftJar;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class TileEntityRendererRiftJar extends TileEntitySpecialRenderer<TileEntityRiftJar> {

    private final ModelBase model = new ModelRiftJar();
    private final ResourceLocation texture = new ResourceLocation(Tags.MOD_ID, "textures/blocks/rift_jar.png");
    private final ResourceLocation crackedTexture = new ResourceLocation(Tags.MOD_ID, "textures/blocks/rift_jar_cracked.png");

    @Override
    public void render(TileEntityRiftJar tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        Entity fakeEntity = new EntityTippedArrow(tileEntity.getWorld());
        fakeEntity.rotationYaw = tileEntity.fractionOpen;
        fakeEntity.rotationPitch = tileEntity.fractionHandleUp
            * (1.0f + MathHelper.sin(tileEntity.ticksAliveVisual * 0.1f) * 0.1f);

        GlStateManager.pushMatrix();
        GlStateManager.rotate(-90.0f * tileEntity.blockRotation + 270.0f, 0.0f, 1.0f, 0.0f);

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
    }
}
