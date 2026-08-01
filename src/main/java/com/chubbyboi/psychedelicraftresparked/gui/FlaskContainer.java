package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityFlask;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class FlaskContainer extends Container {

    public static final int TOGGLE_DIRECTION_BUTTON_ID = 0;

    private final TileEntityFlask tileentity;
    private int fluidAmount;
    private boolean drainingMode;

    public FlaskContainer(InventoryPlayer player, TileEntityFlask tileentity) {
        this.tileentity = tileentity;

        this.addSlotToContainer(new Slot(tileentity, TileEntityFlask.FLUID_IO_SLOT, 25, 40));

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

    public TileEntityFlask getTileEntity() {
        return tileentity;
    }

    @Override
    public boolean enchantItem(EntityPlayer playerIn, int id) {
        if (id == TOGGLE_DIRECTION_BUTTON_ID) {
            tileentity.toggleDrainingMode();
            return true;
        }
        return false;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, this.tileentity);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.fluidAmount != this.tileentity.getField(0)) listener.sendWindowProperty(this, 0, this.tileentity.getField(0));
            if (this.drainingMode != (this.tileentity.getField(1) != 0)) listener.sendWindowProperty(this, 1, this.tileentity.getField(1));
        }

        this.fluidAmount = this.tileentity.getField(0);
        this.drainingMode = this.tileentity.getField(1) != 0;
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

            // Slot layout: 0 fluid IO, 1-27 player inventory, 28-36 hotbar.
            if (index == 0) {
                if (!this.mergeItemStack(itemStack1, 1, 37, false)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(itemStack1, itemStack);
            } else if (index < 28) {
                if (!this.mergeItemStack(itemStack1, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemStack1, 1, 28, false)) {
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