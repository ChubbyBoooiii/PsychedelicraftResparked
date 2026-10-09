package com.chubbyboi.psychedelicraftresparked.tileentities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class TileEntityBottleWorkbench extends TileEntity implements IInventory {

    public static final int GLASS_SLOT = 0;
    public static final int BOTTLE_DYE_SLOT = 1;
    public static final int BOTTLES_SLOT = 2;
    public static final int PAPER_SLOT = 3;
    public static final int LABEL_DYE_SLOT = 4;

    public static final int MODE_BOTTLES = 0;
    public static final int MODE_LABELS = 1;

    private static final int FIELD_MODE = 0;
    private static final int FIELD_BOTTLE_SHAPE = 1;
    private static final int FIELD_LABEL_SHAPE = 2;

    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int mode = MODE_BOTTLES;
    private int bottleShape = -1;
    private int labelShape = -1;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
        markDirty();
    }

    public int getBottleShape() {
        return bottleShape;
    }

    public void setBottleShape(int bottleShape) {
        this.bottleShape = bottleShape;
        markDirty();
    }

    public int getLabelShape() {
        return labelShape;
    }

    public void setLabelShape(int labelShape) {
        this.labelShape = labelShape;
        markDirty();
    }

    @Override
    public String getName() {
        return "container.bottle_workbench";
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
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack result = ItemStackHelper.getAndRemove(items, index);
        markDirty();
        return result;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        items.set(index, stack);
        if (stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    // Slot rules live on the container's slots
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        switch (id) {
            case FIELD_MODE:
                return mode;
            case FIELD_BOTTLE_SHAPE:
                return bottleShape;
            case FIELD_LABEL_SHAPE:
                return labelShape;
            default:
                return 0;
        }
    }

    @Override
    public void setField(int id, int value) {
        switch (id) {
            case FIELD_MODE:
                mode = value;
                break;
            case FIELD_BOTTLE_SHAPE:
                bottleShape = value;
                break;
            case FIELD_LABEL_SHAPE:
                labelShape = value;
                break;
        }
    }

    @Override
    public int getFieldCount() {
        return 3;
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, items);
        compound.setInteger("Mode", mode);
        compound.setInteger("BottleShape", bottleShape);
        compound.setInteger("LabelShape", labelShape);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        items = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, items);
        mode = compound.getInteger("Mode");
        bottleShape = compound.hasKey("BottleShape") ? compound.getInteger("BottleShape") : -1;
        labelShape = compound.hasKey("LabelShape") ? compound.getInteger("LabelShape") : -1;
    }
}