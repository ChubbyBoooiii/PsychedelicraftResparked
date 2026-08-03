package com.chubbyboi.psychedelicraftresparked.tileentities;

import com.chubbyboi.psychedelicraftresparked.fluids.FermentableFluid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;

public class TileEntityBarrel extends TileEntity implements ITickable {
    public static final int CAPACITY = 16000;

    // topping up dilutes progress, draining fully resets it.
    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        public int fill(FluidStack resource, boolean doFill) {
            int filled = super.fill(resource, doFill);
            if (doFill && filled > 0) {
                double amountFilled = (double) filled / (double) getFluidAmount();
                timeFermented = (int) Math.floor(timeFermented * (1.0 - amountFilled));
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            FluidStack drained = super.drain(resource, doDrain);
            if (doDrain && drained != null && getFluidAmount() == 0) {
                timeFermented = 0;
            }
            return drained;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            FluidStack drained = super.drain(maxDrain, doDrain);
            if (doDrain && drained != null && getFluidAmount() == 0) {
                timeFermented = 0;
            }
            return drained;
        }
    };

    private int rotation;

    private float tapRotation;
    private int timeLeftTapOpen;

    private int timeFermented;

    private boolean sealed;
    private boolean hasTap;

    @Override
    public void update() {
        if (timeLeftTapOpen > 0) {
            timeLeftTapOpen--;
        }
        if (timeLeftTapOpen > 0 && tapRotation < ((float) Math.PI) * 0.5F) {
            tapRotation += ((float) Math.PI) * 0.1F;
        }
        if (timeLeftTapOpen == 0 && tapRotation > 0.0F) {
            tapRotation -= ((float) Math.PI) * 0.1F;
        }

        tickMaturation();
    }

    public void openTap() {
        timeLeftTapOpen = 20;
    }

    private void tickMaturation() {
        if (!sealed || hasTap) {
            return;
        }

        int neededMaturationTime = getNeededMaturationTime();
        if (neededMaturationTime < 0) {
            return;
        }

        if (timeFermented >= neededMaturationTime) {
            if (!world.isRemote) {
                FermentableFluid fermentable = (FermentableFluid) tank.getFluid().getFluid();
                fermentable.fermentStep(tank.getFluid(), false);
                timeFermented = 0;
                markDirty();
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
            }
        } else {
            timeFermented++;
        }
    }

    public int getTimeFermented() {
        return timeFermented;
    }

    public int getNeededMaturationTime() {
        FluidStack fluidStack = tank.getFluid();
        if (fluidStack == null || !(fluidStack.getFluid() instanceof FermentableFluid)) {
            return -1;
        }
        return ((FermentableFluid) fluidStack.getFluid()).fermentationTime(fluidStack, false);
    }

    public boolean isMaturing() {
        return sealed && !hasTap && getNeededMaturationTime() >= 0;
    }

    public boolean isSealed() {
        return sealed;
    }

    public void setSealed(boolean sealed) {
        this.sealed = sealed;
        markDirty();
    }

    public void toggleSealed() {
        setSealed(!sealed);
    }

    public boolean hasTap() {
        return hasTap;
    }

    public void attachTap() {
        hasTap = true;
        markDirty();
    }

    public FluidTank getTank() {
        return tank;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation & 3;
    }

    public float getTapRotation() {
        return tapRotation;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    // ==================== Capabilities ====================

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return !sealed;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return sealed ? null : (T) tank;
        }
        return super.getCapability(capability, facing);
    }

    // ==================== NBT ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Tank", tank.writeToNBT(new NBTTagCompound()));
        compound.setInteger("Rotation", rotation);
        compound.setFloat("TapRotation", tapRotation);
        compound.setInteger("TimeLeftTapOpen", timeLeftTapOpen);
        compound.setInteger("TimeFermented", timeFermented);
        compound.setBoolean("Sealed", sealed);
        compound.setBoolean("HasTap", hasTap);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        tank.readFromNBT(compound.getCompoundTag("Tank"));
        rotation = compound.getInteger("Rotation");
        tapRotation = compound.getFloat("TapRotation");
        timeLeftTapOpen = compound.getInteger("TimeLeftTapOpen");
        timeFermented = compound.getInteger("TimeFermented");
        sealed = compound.getBoolean("Sealed");
        hasTap = compound.getBoolean("HasTap");
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    // ==================== Container window properties ====================

    public int getField(int id) {
        switch (id) {
            case 0: return tank.getFluidAmount();
            case 1: return sealed ? 1 : 0;
            case 2: return timeFermented;
            case 3: return hasTap ? 1 : 0;
            default: return 0;
        }
    }

    public void setField(int id, int value) {
        if (id == 1) {
            sealed = value != 0;
        } else if (id == 2) {
            timeFermented = value;
        } else if (id == 3) {
            hasTap = value != 0;
        }
    }

    public static final int FIELD_COUNT = 4;
}