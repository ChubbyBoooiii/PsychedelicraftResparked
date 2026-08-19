package com.chubbyboi.psychedelicraftresparked.mixins;

import com.chubbyboi.psychedelicraftresparked.init.AdvancementInit;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public class MixinEntityPlayerMP {

    @Inject(
        method = "sendSlotContents",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancements/critereon/InventoryChangeTrigger;trigger(Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/entity/player/InventoryPlayer;)V",
            shift = At.Shift.AFTER
        ),
        require = 1
    )
    private void onInventoryChanged(CallbackInfo ci) {
        EntityPlayerMP player = (EntityPlayerMP) (Object) this;
        AdvancementInit.HELD_FLUID.trigger(player);
        AdvancementInit.TOTAL_ITEM_COUNT.trigger(player);
    }
}