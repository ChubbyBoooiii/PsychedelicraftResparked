package com.chubbyboi.psychedelicraftresparked.gui;

import com.chubbyboi.psychedelicraftresparked.block.ContainerShape;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.recipes.BottleWorkbenchRecipes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class BottleWorkbenchContainer extends Container {

    public static final int GLASS_SLOT = 0;
    public static final int DYE_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    private static final int PLAYER_START = 3;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final World world;
    private final BlockPos pos;
    private final InventoryBasic inputs = new InventoryBasic("bottle_workbench", false, 2);
    private final InventoryBasic output = new InventoryBasic("bottle_workbench_output", false, 1);
    private int selectedShape = -1;

    public BottleWorkbenchContainer(InventoryPlayer player, World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
        inputs.addInventoryChangeListener(inventory -> onCraftMatrixChanged(inventory));

        addSlotToContainer(new Slot(inputs, 0, 13, 32) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return BottleWorkbenchRecipes.isGlass(stack);
            }
        });
        addSlotToContainer(new Slot(inputs, 1, 33, 32) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return BottleWorkbenchRecipes.isDye(stack);
            }
        });
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
    }

    public int getSelectedShape() {
        return selectedShape;
    }

    public ItemStack getGlass() {
        return inputs.getStackInSlot(0);
    }

    public ItemStack getDye() {
        return inputs.getStackInSlot(1);
    }

    @Override
    public boolean enchantItem(EntityPlayer playerIn, int id) {
        if (id < 0 || id >= BottleWorkbenchRecipes.getShapes().size()) {
            return false;
        }
        selectedShape = id;
        updateOutput();
        return true;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        updateOutput();
    }

    private void updateOutput() {
        List<ContainerShape> shapes = BottleWorkbenchRecipes.getShapes();
        ItemStack result = selectedShape >= 0 && selectedShape < shapes.size()
            ? BottleWorkbenchRecipes.getResult(getGlass(), getDye(), shapes.get(selectedShape))
            : ItemStack.EMPTY;
        output.setInventorySlotContents(0, result);
    }

    private void consumeInputs() {
        inputs.decrStackSize(0, 1);
        if (!getDye().isEmpty()) {
            inputs.decrStackSize(1, 1);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return world.getBlockState(pos).getBlock() == BlockInit.BOTTLE_WORKBENCH
            && playerIn.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!world.isRemote) {
            clearContainer(playerIn, world, inputs);
        }
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
        } else if (BottleWorkbenchRecipes.isGlass(stack)) {
            if (!mergeItemStack(stack, GLASS_SLOT, GLASS_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (BottleWorkbenchRecipes.isDye(stack)) {
            if (!mergeItemStack(stack, DYE_SLOT, DYE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        return original;
    }
}