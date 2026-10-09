package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import net.minecraft.block.BlockPlanks;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

public class TileEntityItemStackRendererBarrel extends TileEntityItemStackRenderer {

    @Override
    public void renderByItem(ItemStack itemStackIn) {
        TileEntityRendererBarrel.render(BlockPlanks.EnumType.byMetadata(itemStackIn.getMetadata()), false, 0.0F);
    }
}