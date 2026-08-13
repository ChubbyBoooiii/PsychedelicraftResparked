package com.chubbyboi.psychedelicraftresparked.mixins;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundManager.class)
public class MixinSoundManager {

    @Inject(method = "getClampedVolume", at = @At("RETURN"), cancellable = true)
    private void applyDrugSoundVolume(ISound soundIn, CallbackInfoReturnable<Float> cir) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }

        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) {
            return;
        }

        float modifier = ((DrugProperties) props).getSoundVolumeModifier();
        if (modifier >= 0.999f) {
            return;
        }

        cir.setReturnValue(cir.getReturnValueF() * modifier);
    }
}