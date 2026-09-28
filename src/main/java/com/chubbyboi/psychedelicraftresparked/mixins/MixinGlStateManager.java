package com.chubbyboi.psychedelicraftresparked.mixins;

import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.WorldShaderEffect;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {

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

    @Inject(method = "enableLight", at = @At("HEAD"))
    private static void onEnableLight(int light, CallbackInfo ci) {
        WorldShaderEffect.getInstance().setLightEnabled(light, true);
    }

    @Inject(method = "disableLight", at = @At("HEAD"))
    private static void onDisableLight(int light, CallbackInfo ci) {
        WorldShaderEffect.getInstance().setLightEnabled(light, false);
    }

    @Inject(method = "enableColorMaterial", at = @At("HEAD"))
    private static void onEnableColorMaterial(CallbackInfo ci) {
        WorldShaderEffect.getInstance().setColorMaterialEnabled(true);
    }

    @Inject(method = "disableColorMaterial", at = @At("HEAD"))
    private static void onDisableColorMaterial(CallbackInfo ci) {
        WorldShaderEffect.getInstance().setColorMaterialEnabled(false);
    }

    @Inject(method = "colorMaterial", at = @At("HEAD"))
    private static void onColorMaterial(int face, int mode, CallbackInfo ci) {
        WorldShaderEffect.getInstance().setColorMaterialMode(mode);
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