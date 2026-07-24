package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.block.BlockPeyote;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityPeyote;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class TileEntityRendererPeyote extends TileEntitySpecialRenderer<TileEntityPeyote> {

    private final ModelBase[] peyoteModels = new ModelBase[]{
        new ModelPeyote0(),
        new ModelPeyote1(),
        new ModelPeyote2(),
        new ModelPeyote3()
    };

    private final ResourceLocation[] blockTextures = new ResourceLocation[]{
        new ResourceLocation("psychedelicraftresparked:textures/entities/peyote_0.png"),
        new ResourceLocation("psychedelicraftresparked:textures/entities/peyote_1.png"),
        new ResourceLocation("psychedelicraftresparked:textures/entities/peyote_2.png"),
        new ResourceLocation("psychedelicraftresparked:textures/entities/peyote_3.png")
    };

    @Override
    public void render(TileEntityPeyote tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        int age = tileEntity.getWorld().getBlockState(tileEntity.getPos()).getValue(BlockPeyote.AGE);
        if (age > 3) {
            age = 3;
        }

        bindTexture(blockTextures[age]);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 1.5D, z + 0.5D);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);

        BlockPos pos = tileEntity.getPos();
        float uniqueRotation = pos.getX() * 18249.849231F + pos.getY() * 892308.237542F + pos.getZ() * 4387598.23842F;
        GlStateManager.rotate(uniqueRotation % 360.0F, 0.0F, 1.0F, 0.0F);

        peyoteModels[age].render(null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

        GlStateManager.popMatrix();
    }
}