package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.block.ContainerShape;
import com.chubbyboi.psychedelicraftresparked.item.ItemBottle;
import com.chubbyboi.psychedelicraftresparked.recipes.BottleWorkbenchRecipes;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBottleWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Predicate;

public class BottleWorkbenchContainer extends Container {

    public static final int GLASS_SLOT = TileEntityBottleWorkbench.GLASS_SLOT;
    public static final int BOTTLE_DYE_SLOT = TileEntityBottleWorkbench.BOTTLE_DYE_SLOT;
    public static final int BOTTLES_SLOT = TileEntityBottleWorkbench.BOTTLES_SLOT;
    public static final int PAPER_SLOT = TileEntityBottleWorkbench.PAPER_SLOT;
    public static final int LABEL_DYE_SLOT = TileEntityBottleWorkbench.LABEL_DYE_SLOT;
    public static final int OUTPUT_SLOT = 5;
    private static final int PLAYER_START = 6;
    private static final int PLAYER_END = PLAYER_START + 36;
    public static final int MAX_LABELLED_BOTTLES = 8;

    public static final int TAB_BOTTLES_ID = 100;
    public static final int TAB_LABELS_ID = 101;

    private final TileEntityBottleWorkbench tileentity;
    private final InventoryBasic output = new InventoryBasic("bottle_workbench_output", false, 1);
    private int lastMode, lastBottleShape, lastLabelShape;

    public BottleWorkbenchContainer(InventoryPlayer player, TileEntityBottleWorkbench tileentity) {
        this.tileentity = tileentity;

        addSlotToContainer(new InputSlot(GLASS_SLOT, 13, 32, TileEntityBottleWorkbench.MODE_BOTTLES, BottleWorkbenchRecipes::isGlass, 64));
        addSlotToContainer(new InputSlot(BOTTLE_DYE_SLOT, 33, 32, TileEntityBottleWorkbench.MODE_BOTTLES, BottleWorkbenchRecipes::isDye, 64));
        addSlotToContainer(new InputSlot(BOTTLES_SLOT, 23, 18, TileEntityBottleWorkbench.MODE_LABELS, stack -> stack.getItem() instanceof ItemBottle, MAX_LABELLED_BOTTLES));
        addSlotToContainer(new InputSlot(PAPER_SLOT, 13, 48, TileEntityBottleWorkbench.MODE_LABELS, stack -> stack.getItem() == Items.PAPER, 64));
        addSlotToContainer(new InputSlot(LABEL_DYE_SLOT, 33, 48, TileEntityBottleWorkbench.MODE_LABELS, BottleWorkbenchRecipes::isDye, 64));
        addSlotToContainer(new Slot(output, 0, 143, 57) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
                consumeInputs();
                return super.onTake(thePlayer, stack);
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlotToContainer(new Slot(player, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column) {
            addSlotToContainer(new Slot(player, column, 8 + column * 18, 142));
        }

        updateOutput();
    }

    public int getMode() {
        return tileentity.getMode();
    }

    public int getSelectedShape() {
        return tileentity.getBottleShape();
    }

    public ItemStack getGlass() {
        return tileentity.getStackInSlot(GLASS_SLOT);
    }

    public ItemStack getDye() {
        return tileentity.getStackInSlot(BOTTLE_DYE_SLOT);
    }

    public ItemStack getStackInInput(int index) {
        return tileentity.getStackInSlot(index);
    }

    @Override
    public boolean enchantItem(EntityPlayer playerIn, int id) {
        if (id == TAB_BOTTLES_ID || id == TAB_LABELS_ID) {
            tileentity.setMode(id == TAB_BOTTLES_ID ? TileEntityBottleWorkbench.MODE_BOTTLES : TileEntityBottleWorkbench.MODE_LABELS);
        } else if (id >= 0 && id < BottleWorkbenchRecipes.getShapes().size() && getMode() == TileEntityBottleWorkbench.MODE_BOTTLES) {
            tileentity.setBottleShape(id);
        } else {
            return false;
        }
        updateOutput();
        return true;
    }

    private void updateOutput() {
        ItemStack result = ItemStack.EMPTY;
        if (getMode() == TileEntityBottleWorkbench.MODE_BOTTLES) {
            List<ContainerShape> shapes = BottleWorkbenchRecipes.getShapes();
            int selectedShape = getSelectedShape();
            if (selectedShape >= 0 && selectedShape < shapes.size()) {
                result = BottleWorkbenchRecipes.getResult(getGlass(), getDye(), shapes.get(selectedShape));
            }
        }
        output.setInventorySlotContents(0, result);
    }

    private void consumeInputs() {
        tileentity.decrStackSize(GLASS_SLOT, 1);
        if (!getDye().isEmpty()) {
            tileentity.decrStackSize(BOTTLE_DYE_SLOT, 1);
        }
        updateOutput();
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, tileentity);
    }

    @Override
    public void detectAndSendChanges() {
        // Someone else at the same workbench may have changed the inputs or the mode
        updateOutput();
        super.detectAndSendChanges();

        for (IContainerListener listener : listeners) {
            if (lastMode != tileentity.getField(0)) listener.sendWindowProperty(this, 0, tileentity.getField(0));
            if (lastBottleShape != tileentity.getField(1)) listener.sendWindowProperty(this, 1, tileentity.getField(1));
            if (lastLabelShape != tileentity.getField(2)) listener.sendWindowProperty(this, 2, tileentity.getField(2));
        }
        lastMode = tileentity.getField(0);
        lastBottleShape = tileentity.getField(1);
        lastLabelShape = tileentity.getField(2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        tileentity.setField(id, data);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tileentity.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        if (index == OUTPUT_SLOT) {
            // Make bottles until a resource runs out: with a dye in, stop when the dye runs out rather than carrying on with the glass's own colour
            boolean usingDye = !getDye().isEmpty();
            while (slot.getHasStack() && getDye().isEmpty() != usingDye) {
                ItemStack made = slot.getStack().copy();
                if (!mergeItemStack(made, PLAYER_START, PLAYER_END, true)) {
                    break;
                }
                slot.onTake(playerIn, slot.getStack().copy());
            }
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (index < PLAYER_START) {
            if (!mergeItemStack(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveToInput(stack)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        return original;
    }

    private boolean moveToInput(ItemStack stack) {
        for (int i = GLASS_SLOT; i <= LABEL_DYE_SLOT; i++) {
            Slot input = inventorySlots.get(i);
            if (!input.isItemValid(stack)) {
                continue;
            }
            ItemStack existing = input.getStack();
            int limit = input.getItemStackLimit(stack);
            if (existing.isEmpty()) {
                input.putStack(stack.splitStack(Math.min(stack.getCount(), limit)));
                return true;
            }
            if (ItemStack.areItemsEqual(existing, stack) && ItemStack.areItemStackTagsEqual(existing, stack)) {
                int moved = Math.min(stack.getCount(), Math.min(limit, existing.getMaxStackSize()) - existing.getCount());
                if (moved <= 0) {
                    return false;
                }
                existing.grow(moved);
                stack.shrink(moved);
                input.onSlotChanged();
                return true;
            }
            return false;
        }
        return false;
    }

    private class InputSlot extends Slot {

        private final int mode;
        private final Predicate<ItemStack> valid;
        private final int limit;

        InputSlot(int index, int x, int y, int mode, Predicate<ItemStack> valid, int limit) {
            super(tileentity, index, x, y);
            this.mode = mode;
            this.valid = valid;
            this.limit = limit;
        }

        private boolean isActive() {
            return getMode() == mode;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return isActive() && valid.test(stack);
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return isActive();
        }

        @Override
        public int getSlotStackLimit() {
            return limit;
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();
            updateOutput();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public boolean isEnabled() {
            return isActive();
        }
    }
}