package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class VatInputSlot extends Slot {

    private final TileEntityVat tileentity;

    public VatInputSlot(TileEntityVat tileentity, int index, int x, int y) {
        super(tileentity, index, x, y);
        this.tileentity = tileentity;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !tileentity.isInputLocked();
    }

    @Override
    public boolean isEnabled() {
        return !tileentity.isInputLocked();
    }
}