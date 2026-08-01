package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBarrel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BarrelContainer extends Container {

    public static final int SEAL_BUTTON_ID = 0;

    private final TileEntityBarrel tileentity;
    private int fluidAmount;
    private boolean sealed;
    private int timeFermented;
    private boolean hasTap;

    public BarrelContainer(InventoryPlayer player, TileEntityBarrel tileentity) {
        this.tileentity = tileentity;

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Hotbar slots
        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(player, k, 8 + k * 18, 142));
        }
    }

    public TileEntityBarrel getTileEntity() {
        return tileentity;
    }

    @Override
    public boolean enchantItem(EntityPlayer playerIn, int id) {
        if (id == SEAL_BUTTON_ID) {
            tileentity.toggleSealed();
            return true;
        }
        return false;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        for (int i = 0; i < TileEntityBarrel.FIELD_COUNT; i++) {
            listener.sendWindowProperty(this, i, tileentity.getField(i));
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.fluidAmount != this.tileentity.getField(0)) listener.sendWindowProperty(this, 0, this.tileentity.getField(0));
            if (this.sealed != (this.tileentity.getField(1) != 0)) listener.sendWindowProperty(this, 1, this.tileentity.getField(1));
            if (this.timeFermented != this.tileentity.getField(2)) listener.sendWindowProperty(this, 2, this.tileentity.getField(2));
            if (this.hasTap != (this.tileentity.getField(3) != 0)) listener.sendWindowProperty(this, 3, this.tileentity.getField(3));
        }

        this.fluidAmount = this.tileentity.getField(0);
        this.sealed = this.tileentity.getField(1) != 0;
        this.timeFermented = this.tileentity.getField(2);
        this.hasTap = this.tileentity.getField(3) != 0;
    }

    @Override
    public void updateProgressBar(int id, int data) {
        this.tileentity.setField(id, data);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.tileentity.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack1 = slot.getStack();
            itemStack = itemStack1.copy();

            // Slot layout: 0-26 player inventory, 27-35 hotbar (no fluid IO slot in this redesign).
            if (index < 27) {
                if (!this.mergeItemStack(itemStack1, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemStack1, 0, 27, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack1.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemStack1.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemStack1);
        }
        return itemStack;
    }
}