package com.chubbyboi.psychedelicraftresparked.tileentities;

import com.chubbyboi.psychedelicraftresparked.block.BlockVat;
import com.chubbyboi.psychedelicraftresparked.fluids.FermentableFluid;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.network.NetworkHandler;
import com.chubbyboi.psychedelicraftresparked.network.PacketSpawnFluidSplash;
import com.chubbyboi.psychedelicraftresparked.recipes.VatRecipes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;

public class TileEntityVat extends TileEntity implements ITickable, ISidedInventory {
    public static final int CAPACITY = 16000;
    public static final int INGREDIENT_SLOTS = 7;
    public static final int FLUID_IO_SLOT = 7;
    public static final int SLOT_COUNT = 8;

    public static final String OUTPUT_ITEM_TAG = "psychedelicraftresparked_vat_output";

    public static int MIXING_TIME = 100; // 5 seconds

    private static final int[] SLOTS = new int[SLOT_COUNT];
    static {
        for (int i = 0; i < SLOT_COUNT; i++) SLOTS[i] = i;
    }

    private final FluidTank tank = new FluidTank(CAPACITY);
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public boolean mixing;
    public int mixingProgress;
    public int totalMixingTime;

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

        if (mixing) {
            if (tickMixing()) {
                dirty = true;
            }
        } else if (fermenting) {
            if (tickFermentation()) {
                dirty = true;
            }
        } else {
            if (!isInputLocked() && collectNearbyItems()) {
                dirty = true;
            }
            if (!beginMixing() && beginHardeningIfApplicable()) {
                dirty = true;
            }
        }

        if (dirty) {
            markDirty();
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    public boolean isInputLocked() {
        if (!solidContents.isEmpty() || mixing) {
            return true;
        }
        FluidStack fluid = tank.getFluid();
        return fluid != null && fluid.amount > 0 && !VatRecipes.getInstance().isRawInput(fluid.getFluid());
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
        if (stack.isEmpty() || stack.getCount() != 1) {
            return false;
        }

        if (stack.getItem() == Items.WATER_BUCKET || stack.getItem() == Items.MILK_BUCKET || stack.getItem() == Items.BUCKET) {
            return processBucket(stack);
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

    private boolean processBucket(ItemStack stack) {
        if (drainingMode) {
            // Drain: a full bucket's worth of fluid moves from the held bucket into the tank.
            Fluid bucketFluid = FluidHelper.getBucketFluid(stack.getItem());
            if (bucketFluid == null) {
                return false;
            }
            FluidStack fluidStack = new FluidStack(bucketFluid, FluidHelper.BUCKET_VOLUME);
            if (tank.fill(fluidStack, false) < fluidStack.amount) {
                return false;
            }
            tank.fill(fluidStack, true);
            items.set(FLUID_IO_SLOT, new ItemStack(Items.BUCKET));
            return true;
        }

        if (stack.getItem() != Items.BUCKET) {
            return false;
        }
        FluidStack tankFluid = tank.getFluid();
        if (tankFluid == null || tankFluid.amount < FluidHelper.BUCKET_VOLUME) {
            return false;
        }
        Item filledBucket = FluidHelper.getFilledBucket(tankFluid.getFluid());
        if (filledBucket == null) {
            return false;
        }
        tank.drain(FluidHelper.BUCKET_VOLUME, true);
        items.set(FLUID_IO_SLOT, new ItemStack(filledBucket));
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

    private boolean beginMixing() {
        if (mixing || world.isRemote) {
            return false;
        }

        VatRecipes.Recipe recipe = VatRecipes.getInstance().findMatch(tank, items);
        if (recipe == null) {
            return false;
        }

        mixing = true;
        mixingProgress = 0;
        totalMixingTime = MIXING_TIME;

        markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        return true;
    }

    private boolean tickMixing() {
        mixingProgress++;

        // Do a little bubble and splash, not at the end cause the water bubbles dont match the booze colour
        if (mixingProgress % 15 == 0 && mixingProgress < totalMixingTime - 15) {
            spawnMixingBubbles();
        }

        if (mixingProgress >= totalMixingTime) {
            mixing = false;

            VatRecipes.Recipe recipe = VatRecipes.getInstance().findMatch(tank, items);
            if (recipe != null) {
                recipe.consumeIngredients(items);
                tank.setFluid(new FluidStack(recipe.getOutput(), tank.getFluidAmount()));

                fermenting = true;
                fermentationProgress = 0;
                totalFermentationTime = getFermentationTimeNeeded();
            }
        }
        return true;
    }

    private void spawnMixingBubbles() {
        FluidStack fluidStack = tank.getFluid();
        if (fluidStack == null || fluidStack.amount <= 0) {
            return;
        }

        AxisAlignedBB fluidBox = BlockVat.getVatFluidBoundingBox(world, pos);
        if (fluidBox == null) {
            return;
        }

        // Inset from the fluid box's edges so bubbles dont clip through
        double marginX = (fluidBox.maxX - fluidBox.minX) * 0.25D;
        double marginZ = (fluidBox.maxZ - fluidBox.minZ) * 0.25D;
        double spawnX = (fluidBox.minX + marginX) + world.rand.nextDouble() * ((fluidBox.maxX - marginX) - (fluidBox.minX + marginX));
        double spawnZ = (fluidBox.minZ + marginZ) + world.rand.nextDouble() * ((fluidBox.maxZ - marginZ) - (fluidBox.minZ + marginZ));
        double spawnY = fluidBox.maxY;

        int color = FluidHelper.getDisplayColor(fluidStack);
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        PacketSpawnFluidSplash packet = new PacketSpawnFluidSplash(spawnX, spawnY, spawnZ, 0.0D, 0.02D, 0.0D, 0.4F, r, g, b);
        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 32.0D);
        NetworkHandler.INSTANCE.sendToAllAround(packet, point);

        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.4F,
            1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F);
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
        compound.setBoolean("Mixing", mixing);
        compound.setInteger("MixingProgress", mixingProgress);
        compound.setInteger("TotalMixingTime", totalMixingTime);
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
        mixing = compound.getBoolean("Mixing");
        mixingProgress = compound.getInteger("MixingProgress");
        totalMixingTime = compound.getInteger("TotalMixingTime");
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
        return "container.vat";
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
            case 5: return mixing ? 1 : 0;
            case 6: return mixingProgress;
            case 7: return totalMixingTime;
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
            case 5: mixing = value != 0; break;
            case 6: mixingProgress = value; break;
            case 7: totalMixingTime = value; break;
            default: break;
        }
    }

    @Override
    public int getFieldCount() {
        return 8;
    }

    @Override
    public void clear() {
        items.clear();
    }
}