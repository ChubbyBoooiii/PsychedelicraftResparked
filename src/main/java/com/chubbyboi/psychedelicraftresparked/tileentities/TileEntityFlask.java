package com.chubbyboi.psychedelicraftresparked.tileentities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;

import javax.annotation.Nullable;

public class TileEntityFlask extends TileEntity implements ITickable, ISidedInventory {
    public static final int CAPACITY = 8000;
    public static final int FLUID_IO_SLOT = 0;
    public static final int SLOT_COUNT = 1;

    private static final int[] SLOTS = {FLUID_IO_SLOT};

    private final FluidTank tank = new FluidTank(CAPACITY);
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public boolean drainingMode;

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }

        if (processFluidIO()) {
            markDirty();
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    private boolean processFluidIO() {
        ItemStack stack = items.get(FLUID_IO_SLOT);
        if (stack.isEmpty() || stack.getCount() != 1) {
            return false;
        }

        if (stack.getItem() == Items.WATER_BUCKET || stack.getItem() == Items.BUCKET) {
            return processWaterBucket(stack);
        }

        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) {
            return false;
        }

        if (drainingMode) {
            FluidStack simulated = handler.drain(FluidHelper.FLUID_IO_SPEED_PER_TICK, false);
            if (simulated == null || simulated.amount <= 0) {
                return false;
            }
            int accepted = tank.fill(simulated, false);
            if (accepted <= 0) {
                return false;
            }
            FluidStack drained = handler.drain(accepted, true);
            if (drained == null) {
                return false;
            }
            tank.fill(drained, true);
        } else {
            FluidStack simulated = tank.drain(FluidHelper.FLUID_IO_SPEED_PER_TICK, false);
            if (simulated == null || simulated.amount <= 0) {
                return false;
            }
            int accepted = handler.fill(simulated, false);
            if (accepted <= 0) {
                return false;
            }
            FluidStack drained = tank.drain(accepted, true);
            if (drained == null) {
                return false;
            }
            handler.fill(drained, true);
        }

        items.set(FLUID_IO_SLOT, handler.getContainer());
        return true;
    }

    private boolean processWaterBucket(ItemStack stack) {
        if (drainingMode) {
            if (stack.getItem() != Items.WATER_BUCKET) {
                return false;
            }
            FluidStack water = new FluidStack(FluidRegistry.WATER, FluidHelper.BUCKET_VOLUME);
            if (tank.fill(water, false) < water.amount) {
                return false;
            }
            tank.fill(water, true);
            items.set(FLUID_IO_SLOT, new ItemStack(Items.BUCKET));
            return true;
        }

        if (stack.getItem() != Items.BUCKET) {
            return false;
        }
        FluidStack tankFluid = tank.getFluid();
        if (tankFluid == null || tankFluid.getFluid() != FluidRegistry.WATER || tankFluid.amount < FluidHelper.BUCKET_VOLUME) {
            return false;
        }
        tank.drain(FluidHelper.BUCKET_VOLUME, true);
        items.set(FLUID_IO_SLOT, new ItemStack(Items.WATER_BUCKET));
        return true;
    }

    public void toggleDrainingMode() {
        drainingMode = !drainingMode;
        markDirty();
    }

    public FluidTank getTank() {
        return tank;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    // ==================== Capabilities ====================

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) tank;
        }
        return super.getCapability(capability, facing);
    }

    // ==================== NBT ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Tank", tank.writeToNBT(new NBTTagCompound()));
        compound.setBoolean("DrainingMode", drainingMode);
        ItemStackHelper.saveAllItems(compound, items);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        tank.readFromNBT(compound.getCompoundTag("Tank"));
        drainingMode = compound.getBoolean("DrainingMode");
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, items);
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

    // ==================== IInventory / ISidedInventory ====================

    @Override
    public int getSizeInventory() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return items.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack result = ItemStackHelper.getAndSplit(items, index, count);
        markDirty();
        if (!world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack result = ItemStackHelper.getAndRemove(items, index);
        markDirty();
        if (!world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
        return result;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        items.set(index, stack);
        if (stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        markDirty();
        if (!world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) != null
            || stack.getItem() == Items.WATER_BUCKET
            || stack.getItem() == Items.BUCKET;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index == FLUID_IO_SLOT;
    }

    @Override
    public String getName() {
        return "container.flask";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    @Override
    public int getField(int id) {
        switch (id) {
            case 0: return tank.getFluidAmount();
            case 1: return drainingMode ? 1 : 0;
            default: return 0;
        }
    }

    @Override
    public void setField(int id, int value) {
        if (id == 1) {
            drainingMode = value != 0;
        }
    }

    @Override
    public int getFieldCount() {
        return 2;
    }

    @Override
    public void clear() {
        items.clear();
    }
}