package com.chubbyboi.psychedelicraftresparked.mixins;

import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.WorldShaderEffect;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLivingBase.class)
public class MixinRenderLivingBase {

    @Inject(method = "setBrightness", at = @At("RETURN"))
    private void onSetBrightness(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            WorldShaderEffect.getInstance().pauseForUntexturedDraw();
        }
    }

    @Inject(method = "unsetBrightness", at = @At("HEAD"))
    private void onUnsetBrightness(CallbackInfo ci) {
        WorldShaderEffect.getInstance().resumeAfterUntexturedDraw();
    }
}