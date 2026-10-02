package com.chubbyboi.psychedelicraftresparked.mixins;

import com.chubbyboi.psychedelicraftresparked.client.rendering.blocks.TileEntityRendererPlacedContainers;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {

    @Inject(method = "drawBatch", at = @At("TAIL"), remap = false, require = 1)
    private void afterDrawBatch(int pass, CallbackInfo ci) {
        TileEntityRendererPlacedContainers.drawPending();
    }
}