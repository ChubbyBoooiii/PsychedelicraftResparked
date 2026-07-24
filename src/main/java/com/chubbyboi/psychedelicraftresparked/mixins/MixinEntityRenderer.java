package com.chubbyboi.psychedelicraftresparked.mixins;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.client.rendering.SmoothCameraHelper;
import com.chubbyboi.psychedelicraftresparked.client.rendering.shaders.WorldShaderEffect;
import com.chubbyboi.psychedelicraftresparked.drug.IDrug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow @Final private Minecraft mc;

    @Inject(
        method = "updateCameraAndRender",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/profiler/Profiler;endSection()V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void applyDrugCameraDrag(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (!mc.inGameHasFocus) {
            return;
        }

        float inertness = getTotalInertness();
        if (inertness <= 0.001f) {
            return;
        }

        float rawDeltaX = mc.mouseHelper.deltaX;
        float rawDeltaY = mc.mouseHelper.deltaY;
        float speed = SmoothCameraHelper.speedFromSensitivity(mc.gameSettings.mouseSensitivity);
        int sign = mc.gameSettings.invertMouse ? -1 : 1;

        float smoothedYaw = SmoothCameraHelper.getInstance().applyX(rawDeltaX * speed, partialTicks);
        float smoothedPitch = SmoothCameraHelper.getInstance().applyY(rawDeltaY * speed, partialTicks) * sign;

        if (mc.gameSettings.smoothCamera) {
            mc.player.turn(smoothedYaw, smoothedPitch);
        } else {
            float originalYaw = rawDeltaX * speed;
            float originalPitch = rawDeltaY * speed * sign;
            mc.player.turn(smoothedYaw - originalYaw, smoothedPitch - originalPitch);
        }
    }

    @Inject(method = "updateRenderer", at = @At("HEAD"))
    private void onUpdateRenderer(CallbackInfo ci) {
        float inertness = getTotalInertness();
        if (inertness <= 0.001f) {
            return;
        }

        float multiplier = SmoothCameraHelper.multiplierFromInertness(inertness);
        float speed = SmoothCameraHelper.speedFromSensitivity(mc.gameSettings.mouseSensitivity);
        SmoothCameraHelper.getInstance().tick(multiplier * speed);
    }

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void applyDrugViewWobble(float partialTicks, CallbackInfo ci) {
        float wobblyness = getTotalWobblyness();
        if (wobblyness <= 0.001f) {
            return;
        }
        if (wobblyness > 1.0f) {
            wobblyness = 1.0f;
        }

        float time = mc.ingameGUI.getUpdateCounter() + partialTicks;

        float f4 = 5.0f / (wobblyness * wobblyness + 5.0f) - wobblyness * 0.04f;
        f4 *= f4;

        float sin1 = MathHelper.sin(time / 150.0f * (float) Math.PI);
        float sin2 = MathHelper.sin(time / 170.0f * (float) Math.PI);
        float sin3 = MathHelper.sin(time / 190.0f * (float) Math.PI);

        GlStateManager.rotate(time * 3.0f, 0.0f, 1.0f, 1.0f);
        GlStateManager.scale(
            1.0f / (f4 + (wobblyness * sin1) / 2.0f),
            1.0f / (f4 + (wobblyness * sin2) / 2.0f),
            1.0f / (f4 + (wobblyness * sin3) / 2.0f)
        );
        GlStateManager.rotate(-time * 3.0f, 0.0f, 1.0f, 1.0f);
    }

    @Inject(
        method = "updateCameraAndRender",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/EntityRenderer;renderWorld(FJ)V",
            shift = At.Shift.BEFORE
        )
    )
    private void activateWorldShader(float partialTicks, long nanoTime, CallbackInfo ci) {
        WorldShaderEffect.getInstance().activate(partialTicks);
    }

    @Inject(
        method = "updateCameraAndRender",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/EntityRenderer;renderWorld(FJ)V",
            shift = At.Shift.AFTER
        )
    )
    private void deactivateWorldShader(float partialTicks, long nanoTime, CallbackInfo ci) {
        WorldShaderEffect.getInstance().deactivate();
    }

    @Inject(
        method = "renderWorldPass",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;renderSky(FI)V",
            shift = At.Shift.BEFORE
        )
    )
    private void deactivateWorldShaderForSky(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        WorldShaderEffect.getInstance().deactivate();
    }

    @Inject(
        method = "renderWorldPass",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;renderSky(FI)V",
            shift = At.Shift.AFTER
        )
    )
    private void reactivateWorldShaderAfterSky(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        WorldShaderEffect.getInstance().activate(partialTicks);
    }

    private float getTotalWobblyness() {
        EntityPlayerSP player = mc.player;
        if (player == null) {
            return 0.0f;
        }

        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) {
            return 0.0f;
        }

        DrugProperties drugProps = (DrugProperties) props;

        float total = 0.0f;
        for (IDrug drug : drugProps.getAllDrugs()) {
            if (drug.getActiveValue() > 0.001f) {
                total += drug.getViewWobblyness();
            }
        }
        return total;
    }

    private float getTotalInertness() {
        EntityPlayerSP player = mc.player;
        if (player == null) {
            return 0.0f;
        }

        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) {
            return 0.0f;
        }

        DrugProperties drugProps = (DrugProperties) props;

        float totalInertness = 0.0f;
        for (IDrug drug : drugProps.getAllDrugs()) {
            if (drug.getActiveValue() > 0.001f) {
                totalInertness += drug.getHeadMotionInertness();
            }
        }

        if (totalInertness <= 0.001f) {
            SmoothCameraHelper.getInstance().reset();
        }

        return totalInertness;
    }
}
