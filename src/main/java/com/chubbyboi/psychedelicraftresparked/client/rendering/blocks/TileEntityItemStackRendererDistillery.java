package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDistillery;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;

public class TileEntityItemStackRendererDistillery extends TileEntityItemStackRenderer {
    private final TileEntityDistillery distillery = new TileEntityDistillery();

    @Override
    public void renderByItem(ItemStack itemStackIn) {
        TileEntityRendererDispatcher.instance.render(distillery, 0.0D, 0.0D, 0.0D, 0.0F);
    }
}