package com.chubbyboi.psychedelicraftresparked.tileentities;

import com.chubbyboi.psychedelicraftresparked.fluids.FermentableFluid;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.recipes.MashTubRecipes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

public class TileEntityMashTub extends TileEntity implements ITickable, ISidedInventory {
    public static final int CAPACITY = 16000;
    public static final int INGREDIENT_SLOTS = 7;
    public static final int FLUID_IO_SLOT = 7;
    public static final int SLOT_COUNT = 8;

    public static final String OUTPUT_ITEM_TAG = "psychedelicraftresparked_mash_tub_output";

    private static final int[] SLOTS = new int[SLOT_COUNT];
    static {
        for (int i = 0; i < SLOT_COUNT; i++) SLOTS[i] = i;
    }

    private final FluidTank tank = new FluidTank(CAPACITY);
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public boolean fermenting;
    public int fermentationProgress;
    public int totalFermentationTime;
    public boolean drainingMode;

    private ItemStack solidContents = ItemStack.EMPTY;

    private EnumFacing primaryDirection;

    public void setPrimaryDirection(EnumFacing primaryDirection) {
        this.primaryDirection = primaryDirection;
    }

    @Nullable
    public EnumFacing getPrimaryDirection() {
        return primaryDirection;
    }

    @Nullable
    public EnumFacing getSecondaryDirection() {
        return primaryDirection == null ? null : primaryDirection.rotateY();
    }

    public static float rotationFor(EnumFacing primary) {
        if (primary == null) {
            return 0.0F;
        }
        switch (primary) {
            case SOUTH: return 270.0F;
            case WEST: return 180.0F;
            case NORTH: return 90.0F;
            case EAST:
            default: return 0.0F;
        }
    }

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }

        boolean dirty = false;

        if (solidContents.isEmpty() && processFluidIO()) {
            dirty = true;
        }

        if (fermenting) {
            if (tickFermentation()) {
                dirty = true;
            }
        } else {
            if (!isInputLocked() && collectNearbyItems()) {
                dirty = true;
            }
            if (!startFermenting() && beginHardeningIfApplicable()) {
                dirty = true;
            }
        }

        if (dirty) {
            markDirty();
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    public boolean isInputLocked() {
        if (!solidContents.isEmpty()) {
            return true;
        }
        FluidStack fluid = tank.getFluid();
        return fluid != null && fluid.amount > 0 && fluid.getFluid() != FluidRegistry.WATER;
    }

    public ItemStack getSolidContents() {
        return solidContents;
    }

    public void collectSolidContents() {
        solidContents = ItemStack.EMPTY;
        markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    private boolean processFluidIO() {
        ItemStack stack = items.get(FLUID_IO_SLOT);
        if (stack.isEmpty()) {
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
            // Drain: move fluid from the held item into the tank, paced.
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
            // Fill: move fluid from the tank into the held item, paced.
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

    private boolean collectNearbyItems() {
        AxisAlignedBB box = new AxisAlignedBB(
            pos.getX() - 1.0, pos.getY() - 0.1, pos.getZ() - 1.0,
            pos.getX() + 2.0, pos.getY() + 0.4, pos.getZ() + 2.0
        );
        boolean changed = false;
        for (EntityItem entityItem : world.getEntitiesWithinAABB(EntityItem.class, box)) {
            if (!entityItem.isDead && !entityItem.getTags().contains(OUTPUT_ITEM_TAG)) {
                ItemStack before = entityItem.getItem();
                ItemStack remainder = insertIntoIngredientSlots(before);
                if (remainder.getCount() != before.getCount()) {
                    changed = true;
                }
                if (remainder.isEmpty()) {
                    entityItem.setDead();
                } else {
                    entityItem.setItem(remainder);
                }
            }
        }
        return changed;
    }

    private ItemStack insertIntoIngredientSlots(ItemStack stack) {
        stack = stack.copy();
        for (int i = 0; i < INGREDIENT_SLOTS && !stack.isEmpty(); i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack.splitStack(1));
            }
        }
        return stack;
    }

    public boolean startFermenting() {
        if (fermenting || world.isRemote) {
            return false;
        }

        MashTubRecipes.Recipe recipe = MashTubRecipes.getInstance().findMatch(tank, items);
        if (recipe == null) {
            return false;
        }

        recipe.consumeIngredients(items);
        tank.setFluid(new FluidStack(recipe.getOutput(), tank.getFluidAmount()));

        fermenting = true;
        fermentationProgress = 0;
        totalFermentationTime = getFermentationTimeNeeded();

        markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        return true;
    }

    private int getFermentationTimeNeeded() {
        FluidStack stack = tank.getFluid();
        if (stack == null || !(stack.getFluid() instanceof FermentableFluid)) {
            return -1;
        }
        return ((FermentableFluid) stack.getFluid()).fermentationTime(stack, true);
    }

    private boolean beginHardeningIfApplicable() {
        int needed = getFermentationTimeNeeded();
        if (needed < 0) {
            return false;
        }

        fermenting = true;
        fermentationProgress = 0;
        totalFermentationTime = needed;
        return true;
    }

    private boolean tickFermentation() {
        FluidStack stack = tank.getFluid();
        if (stack == null || !(stack.getFluid() instanceof FermentableFluid)) {
            fermenting = false;
            return true;
        }

        FermentableFluid fermentable = (FermentableFluid) stack.getFluid();
        int needed = fermentable.fermentationTime(stack, true);
        if (needed < 0) {
            fermenting = false;
            return true;
        }

        totalFermentationTime = needed;
        fermentationProgress++;
        if (fermentationProgress >= needed) {
            fermentationProgress = 0;
            ItemStack solid = fermentable.fermentStep(stack, true);
            if (solid != null && !solid.isEmpty()) {
                tank.setFluid(null);
                solidContents = solid;
                fermenting = false;
            }
            return true;
        }
        return false;
    }

    public FluidTank getTank() {
        return tank;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    public void toggleDrainingMode() {
        drainingMode = !drainingMode;
        markDirty();
    }

    // ==================== Capabilities ====================

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return solidContents.isEmpty();
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return solidContents.isEmpty() ? (T) tank : null;
        }
        return super.getCapability(capability, facing);
    }

    // ==================== NBT ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Tank", tank.writeToNBT(new NBTTagCompound()));
        compound.setBoolean("Fermenting", fermenting);
        compound.setInteger("FermentationProgress", fermentationProgress);
        compound.setInteger("TotalFermentationTime", totalFermentationTime);
        if (primaryDirection != null) {
            compound.setInteger("PrimaryDirection", primaryDirection.getHorizontalIndex());
        }
        if (!solidContents.isEmpty()) {
            compound.setTag("SolidContents", solidContents.writeToNBT(new NBTTagCompound()));
        }
        ItemStackHelper.saveAllItems(compound, items);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        tank.readFromNBT(compound.getCompoundTag("Tank"));
        fermenting = compound.getBoolean("Fermenting");
        fermentationProgress = compound.getInteger("FermentationProgress");
        totalFermentationTime = compound.getInteger("TotalFermentationTime");
        if (compound.hasKey("PrimaryDirection")) {
            primaryDirection = EnumFacing.byHorizontalIndex(compound.getInteger("PrimaryDirection"));
        }
        solidContents = compound.hasKey("SolidContents")
            ? new ItemStack(compound.getCompoundTag("SolidContents"))
            : ItemStack.EMPTY;
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, items);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket() {
        return new net.minecraft.network.play.server.SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
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
        if (index == FLUID_IO_SLOT) {
            return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) != null
                || stack.getItem() == Items.WATER_BUCKET
                || stack.getItem() == Items.BUCKET;
        }
        return index < INGREDIENT_SLOTS && !isInputLocked();
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
        return "container.mash_tub";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public net.minecraft.util.text.ITextComponent getDisplayName() {
        return new net.minecraft.util.text.TextComponentTranslation(getName());
    }

    @Override
    public int getField(int id) {
        switch (id) {
            case 0: return fermenting ? 1 : 0;
            case 1: return fermentationProgress;
            case 2: return totalFermentationTime;
            case 3: return tank.getFluidAmount();
            case 4: return drainingMode ? 1 : 0;
            default: return 0;
        }
    }

    @Override
    public void setField(int id, int value) {
        switch (id) {
            case 0: fermenting = value != 0; break;
            case 1: fermentationProgress = value; break;
            case 2: totalFermentationTime = value; break;
            case 4: drainingMode = value != 0; break;
            default: break;
        }
    }

    @Override
    public int getFieldCount() {
        return 5;
    }

    @Override
    public void clear() {
        items.clear();
    }
}