package com.chubbyboi.psychedelicraftresparked.mixins;

import com.chubbyboi.psychedelicraftresparked.block.FluidFilled;
import com.chubbyboi.psychedelicraftresparked.network.NetworkHandler;
import com.chubbyboi.psychedelicraftresparked.network.PacketSpawnFluidSplash;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityItem.class)
public abstract class MixinEntityItem {

    private boolean wasInWaterBeforeVanillaCheck;

    @Inject(method = "handleWaterMovement", at = @At("HEAD"))
    private void captureWaterStateBeforeVanillaCheck(CallbackInfoReturnable<Boolean> info) {
        wasInWaterBeforeVanillaCheck = ((EntityAccessor) this).isInWater();
    }

    @Inject(method = "handleWaterMovement", at = @At("RETURN"), cancellable = true)
    private void handleCustomFluidMovement(CallbackInfoReturnable<Boolean> info) {
        Entity self = (Entity) (Object) this;
        EntityAccessor accessor = (EntityAccessor) this;

        if (info.getReturnValueZ() || self.getRidingEntity() instanceof EntityBoat) {
            return;
        }

        AxisAlignedBB entityBox = self.getEntityBoundingBox();

        for (BlockPos.MutableBlockPos checkPos : BlockPos.getAllInBoxMutable(
                new BlockPos(entityBox.minX, entityBox.minY, entityBox.minZ),
                new BlockPos(entityBox.maxX, entityBox.maxY, entityBox.maxZ))) {
            IBlockState state = self.world.getBlockState(checkPos);
            if (!(state.getBlock() instanceof FluidFilled)) {
                continue;
            }

            FluidFilled fluidBlock = (FluidFilled) state.getBlock();
            AxisAlignedBB fluidBox = fluidBlock.getFluidBoundingBox(self.world, checkPos);
            if (fluidBox != null && fluidBox.intersects(entityBox)) {
                if (!wasInWaterBeforeVanillaCheck) {
                    triggerTintedSplashEffect(self, accessor, fluidBlock.getFluidTintColor(self.world, checkPos));
                }
                self.fallDistance = 0.0F;
                accessor.setInWater(true);
                self.extinguish();
                info.setReturnValue(true);
                return;
            }
        }
    }

    private void triggerTintedSplashEffect(Entity self, EntityAccessor accessor, int tintColor) {
        float volume = MathHelper.sqrt(self.motionX * self.motionX * 0.20000000298023224D + self.motionY * self.motionY + self.motionZ * self.motionZ * 0.20000000298023224D) * 0.2F;
        if (volume > 1.0F) {
            volume = 1.0F;
        }
        self.playSound(accessor.callGetSplashSound(), volume, 1.0F + (accessor.getRand().nextFloat() - accessor.getRand().nextFloat()) * 0.4F);

        if (self.world.isRemote) {
            return;
        }

        float r = ((tintColor >> 16) & 0xFF) / 255.0F;
        float g = ((tintColor >> 8) & 0xFF) / 255.0F;
        float b = (tintColor & 0xFF) / 255.0F;
        float surfaceY = (float) MathHelper.floor(self.getEntityBoundingBox().minY) + 1.0F;

        PacketSpawnFluidSplash packet = new PacketSpawnFluidSplash(self.posX, surfaceY, self.posZ, self.motionX, self.motionY, self.motionZ, self.width, r, g, b);
        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(self.dimension, self.posX, self.posY, self.posZ, 64.0);
        NetworkHandler.INSTANCE.sendToAllAround(packet, point);
    }
}