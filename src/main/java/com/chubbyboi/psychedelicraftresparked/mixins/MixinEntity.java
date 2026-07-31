package com.chubbyboi.psychedelicraftresparked.mixins;

import com.chubbyboi.psychedelicraftresparked.block.FluidFilled;
import com.chubbyboi.psychedelicraftresparked.network.NetworkHandler;
import com.chubbyboi.psychedelicraftresparked.network.PacketSpawnFluidSplash;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow public World world;
    @Shadow protected boolean inWater;
    @Shadow public float fallDistance;
    @Shadow protected boolean firstUpdate;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;
    @Shadow public double motionX;
    @Shadow public double motionY;
    @Shadow public double motionZ;
    @Shadow public float width;
    @Shadow public int dimension;
    @Shadow protected Random rand;

    @Shadow public abstract AxisAlignedBB getEntityBoundingBox();
    @Shadow protected abstract SoundEvent getSplashSound();
    @Shadow public abstract void playSound(SoundEvent soundIn, float volume, float pitch);
    @Shadow public abstract void extinguish();
    @Shadow public abstract Entity getRidingEntity();

    private boolean wasInWaterBeforeVanillaCheck;

    @Inject(method = "handleWaterMovement", at = @At("HEAD"))
    private void captureWaterStateBeforeVanillaCheck(CallbackInfoReturnable<Boolean> info) {
        wasInWaterBeforeVanillaCheck = inWater;
    }

    @Inject(method = "handleWaterMovement", at = @At("RETURN"), cancellable = true)
    private void handleCustomFluidMovement(CallbackInfoReturnable<Boolean> info) {
        if (info.getReturnValueZ() || getRidingEntity() instanceof EntityBoat) {
            return;
        }

        AxisAlignedBB entityBox = getEntityBoundingBox();

        for (BlockPos.MutableBlockPos checkPos : BlockPos.getAllInBoxMutable(
                new BlockPos(entityBox.minX, entityBox.minY, entityBox.minZ),
                new BlockPos(entityBox.maxX, entityBox.maxY, entityBox.maxZ))) {
            IBlockState state = world.getBlockState(checkPos);
            if (!(state.getBlock() instanceof FluidFilled)) {
                continue;
            }

            FluidFilled fluidBlock = (FluidFilled) state.getBlock();
            AxisAlignedBB fluidBox = fluidBlock.getFluidBoundingBox(world, checkPos);
            if (fluidBox != null && fluidBox.intersects(entityBox)) {
                if (!wasInWaterBeforeVanillaCheck && !firstUpdate) {
                    triggerTintedSplashEffect(fluidBlock.getFluidTintColor(world, checkPos));
                }
                fallDistance = 0.0F;
                inWater = true;
                extinguish();
                info.setReturnValue(true);
                return;
            }
        }
    }

    private void triggerTintedSplashEffect(int tintColor) {
        float volume = MathHelper.sqrt(motionX * motionX * 0.20000000298023224D + motionY * motionY + motionZ * motionZ * 0.20000000298023224D) * 0.2F;
        if (volume > 1.0F) {
            volume = 1.0F;
        }
        playSound(getSplashSound(), volume, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);

        if (world.isRemote) {
            return;
        }

        float r = ((tintColor >> 16) & 0xFF) / 255.0F;
        float g = ((tintColor >> 8) & 0xFF) / 255.0F;
        float b = (tintColor & 0xFF) / 255.0F;
        float surfaceY = (float) MathHelper.floor(getEntityBoundingBox().minY) + 1.0F;

        PacketSpawnFluidSplash packet = new PacketSpawnFluidSplash(posX, surfaceY, posZ, motionX, motionY, motionZ, width, r, g, b);
        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(dimension, posX, posY, posZ, 64.0);
        NetworkHandler.INSTANCE.sendToAllAround(packet, point);
    }
}