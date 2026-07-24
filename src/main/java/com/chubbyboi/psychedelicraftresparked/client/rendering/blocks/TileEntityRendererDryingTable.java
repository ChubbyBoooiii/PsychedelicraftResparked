package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDryingTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class TileEntityRendererDryingTable extends TileEntitySpecialRenderer<TileEntityDryingTable> {

    @Override
    public void render(TileEntityDryingTable dryingTable, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Random rand = new Random(dryingTable.getPos().toLong());

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.76D, z + 0.5D);
        GlStateManager.scale(0.3F, 0.3F, 0.3F);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-1.5F, -0.0F, -1.0F);

        for (int i = 0; i < dryingTable.getSizeInventory(); i++) {
            if (!dryingTable.getStackInSlot(i).isEmpty()) {
                float posX = randomPos(rand);
                float posZ = randomPos(rand);
                float rotation = randomRotation(rand);
                renderItem(dryingTable, dryingTable.getStackInSlot(i), posX, 0.001F * i, posZ, 0.5F, rotation, partialTicks);
            }
        }
        GlStateManager.popMatrix();
    }

    public void renderItem(TileEntityDryingTable dryingTable, ItemStack stack, float x, float y, float z, float scale, float rotation, float partialTicks) {
        if (!stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.translate(0.75F, 0.0F, 0.25F);
            GlStateManager.rotate(90F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(90F + rotation, 0.0F, 0.0F, 1.0F);
            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, (World) null, (EntityLivingBase) null));
            RenderHelper.enableStandardItemLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    public float randomPos(Random rand) {
        float minXZ = -0.125F;
        float maxXZ = 1.625F;
        return minXZ + rand.nextFloat() * (maxXZ - minXZ);
    }

    public float randomRotation(Random rand) {
        float minRotation = 0;
        float maxRotation = 360;
        return minRotation + rand.nextFloat() * (maxRotation - minRotation);
    }
}