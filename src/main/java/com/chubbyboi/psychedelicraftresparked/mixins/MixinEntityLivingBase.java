package com.chubbyboi.psychedelicraftresparked.mixins;

import com.chubbyboi.psychedelicraftresparked.item.ItemSmokable;
import com.chubbyboi.psychedelicraftresparked.item.PsychItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public class MixinEntityLivingBase {

    @Inject(method = "playEquipSound", at = @At("HEAD"), cancellable = true)
    private void suppressEquipSoundForPsychItems(ItemStack stack, CallbackInfo ci) {
        if (!stack.isEmpty() && stack.getItem() instanceof PsychItem) {
            ci.cancel();
        }
    }

    @Inject(method = "renderBrokenItemStack", at = @At("HEAD"), cancellable = true)
    private void suppressBreakEffectForSmokables(ItemStack stack, CallbackInfo ci) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemSmokable) {
            ci.cancel();
        }
    }
}
