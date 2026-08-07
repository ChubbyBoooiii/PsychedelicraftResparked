package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDryingTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DryingTableContainer extends Container {

    private final TileEntityDryingTable tileentity;
    private int dryingTime, totalDryingTime, progressPerTick, lightPercent, tempPercent;

    public DryingTableContainer(InventoryPlayer player, TileEntityDryingTable tileentity) {
        this.tileentity = tileentity;

        // Add Drying Slots
        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                this.addSlotToContainer(new DryingTableInputSlot(tileentity, x * 3 + y, 30 + x * 18, 17 + y * 18));
            }
        }

        // Add Result Slot
        this.addSlotToContainer(new DryingTableResultSlot(player.player, tileentity, 9, 124, 35));

        // Add Player Inv Slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Add Hotbar Slots
        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(player, k, 8 + k * 18, 142));
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, this.tileentity);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (IContainerListener iContainerListener : this.listeners) {
            IContainerListener listener = iContainerListener;

            if (this.dryingTime != this.tileentity.getField(0))
                listener.sendWindowProperty(this, 0, this.tileentity.getField(0));
            if (this.totalDryingTime != this.tileentity.getField(1))
                listener.sendWindowProperty(this, 1, this.tileentity.getField(1));
            if (this.progressPerTick != this.tileentity.getField(2))
                listener.sendWindowProperty(this, 2, this.tileentity.getField(2));
            if (this.lightPercent != this.tileentity.getField(3))
                listener.sendWindowProperty(this, 3, this.tileentity.getField(3));
            if (this.tempPercent != this.tileentity.getField(4))
                listener.sendWindowProperty(this, 4, this.tileentity.getField(4));
        }

        this.dryingTime = this.tileentity.getField(0);
        this.totalDryingTime = this.tileentity.getField(1);
        this.progressPerTick = this.tileentity.getField(2);
        this.lightPercent = this.tileentity.getField(3);
        this.tempPercent = this.tileentity.getField(4);
    }

    @Override
    @SideOnly(Side.CLIENT)
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

        if(slot != null && slot.getHasStack()) {
            ItemStack itemStack1 = slot.getStack();
            itemStack = itemStack1.copy();

            if (index < 10) {
                if (!this.mergeItemStack(itemStack1, 10, 46, false)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(itemStack1, itemStack);
            } else if (index >= 10 && index < 37) {
                if (!this.mergeItemStack(itemStack1, 0, 9, false) && !this.mergeItemStack(itemStack1, 37, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 37 && index < 46) {
                if (!this.mergeItemStack(itemStack1, 0, 9, false) && !this.mergeItemStack(itemStack1, 10, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemStack1, 0, 9, false) && !this.mergeItemStack(itemStack1, 10, 37, false)) {
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