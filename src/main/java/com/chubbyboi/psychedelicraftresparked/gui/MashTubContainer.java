package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityMashTub;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class MashTubContainer extends Container {

    public static final int START_FERMENTING_BUTTON_ID = 0;
    public static final int TOGGLE_DIRECTION_BUTTON_ID = 1;

    private final TileEntityMashTub tileentity;
    private boolean fermenting;
    private int fermentationProgress;
    private int totalFermentationTime;
    private int fluidAmount;
    private boolean drainingMode;

    public MashTubContainer(InventoryPlayer player, TileEntityMashTub tileentity) {
        this.tileentity = tileentity;

        int[] row1X = {78, 96, 114, 132};
        for (int i = 0; i < row1X.length; i++) {
            this.addSlotToContainer(new MashTubInputSlot(tileentity, i, row1X[i], 20));
        }
        int[] row2X = {87, 105, 123};
        for (int i = 0; i < row2X.length; i++) {
            this.addSlotToContainer(new MashTubInputSlot(tileentity, row1X.length + i, row2X[i], 40));
        }

        this.addSlotToContainer(new Slot(tileentity, TileEntityMashTub.FLUID_IO_SLOT, 25, 40));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 101 + i * 18));
            }
        }

        // Hotbar slots
        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(player, k, 8 + k * 18, 159));
        }
    }

    public TileEntityMashTub getTileEntity() {
        return tileentity;
    }

    @Override
    public boolean enchantItem(EntityPlayer playerIn, int id) {
        if (id == START_FERMENTING_BUTTON_ID) {
            return tileentity.startFermenting();
        }
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
            if (this.fermenting != (this.tileentity.getField(0) != 0)) listener.sendWindowProperty(this, 0, this.tileentity.getField(0));
            if (this.fermentationProgress != this.tileentity.getField(1)) listener.sendWindowProperty(this, 1, this.tileentity.getField(1));
            if (this.totalFermentationTime != this.tileentity.getField(2)) listener.sendWindowProperty(this, 2, this.tileentity.getField(2));
            if (this.fluidAmount != this.tileentity.getField(3)) listener.sendWindowProperty(this, 3, this.tileentity.getField(3));
            if (this.drainingMode != (this.tileentity.getField(4) != 0)) listener.sendWindowProperty(this, 4, this.tileentity.getField(4));
        }

        this.fermenting = this.tileentity.getField(0) != 0;
        this.fermentationProgress = this.tileentity.getField(1);
        this.totalFermentationTime = this.tileentity.getField(2);
        this.fluidAmount = this.tileentity.getField(3);
        this.drainingMode = this.tileentity.getField(4) != 0;
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

            // Slot layout: 0-6 ingredient, 7 fluid, 8-34 player inventory, 35-43 hotbar.
            if (index < 8) {
                if (!this.mergeItemStack(itemStack1, 8, 44, false)) {
                    return ItemStack.EMPTY;
                }
                if (index == 7) {
                    slot.onSlotChange(itemStack1, itemStack);
                }
            } else if (index < 35) {
                if (!this.mergeItemStack(itemStack1, 35, 44, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemStack1, 8, 35, false)) {
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