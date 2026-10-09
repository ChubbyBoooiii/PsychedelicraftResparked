package com.chubbyboi.psychedelicraftresparked.util.compat.universalmodcore.mixins;

import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.WorldShaderEffect;
import org.lwjgl.opengl.ARBShaderObjects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// UMC treats any bound GL program as a shaderpack and then enables normal/specular maps on texture units 2 and 3, which turns its models black under our vertex-only world program.
@Pseudo
@Mixin(targets = {"cam72cam.mod.render.opengl.RenderContext", "cam72cam.mod.render.ShaderHelper"}, remap = false)
public class MixinUmcShaderDetection {

    @Redirect(method = {"apply", "isOptiFineEnabled"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/ARBShaderObjects;glGetHandleARB(I)I"), require = 0, remap = false)
    private static int psychedelicraftresparked$hideWorldShader(int pname) {
        int program = ARBShaderObjects.glGetHandleARB(pname);
        return WorldShaderEffect.getInstance().isOwnProgram(program) ? 0 : program;
    }
}