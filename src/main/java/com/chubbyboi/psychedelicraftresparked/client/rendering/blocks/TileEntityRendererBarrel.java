package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.block.BlockBarrel;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBarrel;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

public class TileEntityRendererBarrel extends TileEntitySpecialRenderer<TileEntityBarrel> {

    private static final Map<BlockPlanks.EnumType, ResourceLocation> TEXTURES = new EnumMap<>(BlockPlanks.EnumType.class);
    static {
        for (BlockPlanks.EnumType type : BlockPlanks.EnumType.values()) {
            TEXTURES.put(type, new ResourceLocation(Tags.MOD_ID, "textures/blocks/barrel_" + type.getName() + ".png"));
        }
    }

    private final ModelBarrel model = new ModelBarrel();

    @Override
    public void render(TileEntityBarrel tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        GlStateManager.rotate(-90.0F * tileEntity.getRotation() + 180.0F, 0.0F, 1.0F, 0.0F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 1.0D, 0.0D);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);

        bindTexture(getTexture(tileEntity));
        model.render(0.0625F, tileEntity.getTapRotation(), tileEntity.hasTap());

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    private ResourceLocation getTexture(TileEntityBarrel tileEntity) {
        IBlockState state = tileEntity.getWorld().getBlockState(tileEntity.getPos());
        BlockPlanks.EnumType wood = state.getPropertyKeys().contains(BlockBarrel.WOOD_TYPE)
            ? state.getValue(BlockBarrel.WOOD_TYPE)
            : BlockPlanks.EnumType.OAK;
        return TEXTURES.get(wood);
    }
}