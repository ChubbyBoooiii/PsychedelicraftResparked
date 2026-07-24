package com.chubbyboi.psychedelicraftresparked.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class DryingTableInputSlot extends Slot {

    public DryingTableInputSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
