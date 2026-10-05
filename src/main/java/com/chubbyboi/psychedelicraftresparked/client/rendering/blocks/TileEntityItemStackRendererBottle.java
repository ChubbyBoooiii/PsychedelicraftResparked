package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

public class TileEntityItemStackRendererBottle extends TileEntityItemStackRenderer {

    @Override
    public void renderByItem(ItemStack itemStackIn) {
        TileEntityRendererPlacedContainers.renderItem(itemStackIn);
    }
}