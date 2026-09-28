package com.chubbyboi.psychedelicraftresparked.mixins;

import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.WorldShaderEffect;
import net.minecraft.client.renderer.OpenGlHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenGlHelper.class)
public class MixinOpenGlHelper {

    @Inject(method = "glUseProgram", at = @At("RETURN"))
    private static void onGlUseProgram(int program, CallbackInfo ci) {
        WorldShaderEffect.getInstance().onExternalProgramChange(program);
    }
}