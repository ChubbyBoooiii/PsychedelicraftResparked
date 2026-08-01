package com.chubbyboi.psychedelicraftresparked.client.rendering.blocks;

import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityFlask;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;

public class TileEntityItemStackRendererFlask extends TileEntityItemStackRenderer {
    private final TileEntityFlask flask = new TileEntityFlask();

    @Override
    public void renderByItem(ItemStack itemStackIn) {
        TileEntityRendererDispatcher.instance.render(flask, 0.0D, 0.0D, 0.0D, 0.0F);
    }
}