package com.chubbyboi.psychedelicraftresparked.mixins;

import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.WorldShaderEffect;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {

    @Inject(method = "disableTexture2D", at = @At("HEAD"))
    private static void onDisableTexture2D(CallbackInfo ci) {
        WorldShaderEffect.getInstance().pauseForUntexturedDraw();
    }

    @Inject(method = "enableTexture2D", at = @At("HEAD"))
    private static void onEnableTexture2D(CallbackInfo ci) {
        WorldShaderEffect.getInstance().resumeAfterUntexturedDraw();
    }

    @Inject(method = "enableTexGenCoord", at = @At("HEAD"))
    private static void onEnableTexGenCoord(GlStateManager.TexGen texGen, CallbackInfo ci) {
        WorldShaderEffect.getInstance().setTexGenEnabled(texGen, true);
    }

    @Inject(method = "disableTexGenCoord", at = @At("HEAD"))
    private static void onDisableTexGenCoord(GlStateManager.TexGen texGen, CallbackInfo ci) {
        WorldShaderEffect.getInstance().setTexGenEnabled(texGen, false);
    }

    @Inject(method = "texGen(Lnet/minecraft/client/renderer/GlStateManager$TexGen;I)V", at = @At("HEAD"))
    private static void onTexGenMode(GlStateManager.TexGen texGen, int mode, CallbackInfo ci) {
        WorldShaderEffect.getInstance().setTexGenMode(texGen, mode);
    }

    @Inject(method = "enableOutlineMode", at = @At("RETURN"))
    private static void onEnableOutlineMode(int color, CallbackInfo ci) {
        WorldShaderEffect.getInstance().setOutlineMode(true);
    }

    @Inject(method = "disableOutlineMode", at = @At("HEAD"))
    private static void onDisableOutlineMode(CallbackInfo ci) {
        WorldShaderEffect.getInstance().setOutlineMode(false);
    }

    @Inject(method = "disableLighting", at = @At("HEAD"))
    private static void onDisableLighting(CallbackInfo ci) {
        WorldShaderEffect.getInstance().setLightingEnabled(false);
    }

    @Inject(method = "enableLighting", at = @At("HEAD"))
    private static void onEnableLighting(CallbackInfo ci) {
        WorldShaderEffect.getInstance().setLightingEnabled(true);
    }
}