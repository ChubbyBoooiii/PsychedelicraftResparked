package com.chubbyboi.psychedelicraftresparked.recipes;

import com.chubbyboi.psychedelicraftresparked.item.ItemDrinkable;
import com.chubbyboi.psychedelicraftresparked.item.ItemMolotovCocktail;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipePourContainer extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private static boolean isPourable(ItemStack stack) {
        return stack.getCount() == 1 && stack.getItem() instanceof ItemDrinkable
            && !(stack.getItem() instanceof ItemMolotovCocktail)
            && ((ItemDrinkable) stack.getItem()).getConsumptionType() == ItemDrinkable.ConsumptionType.DRINK;
    }

    private int[] findPour(InventoryCrafting inv) {
        int firstSlot = -1;
        int secondSlot = -1;
        int otherItems = 0;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (!isPourable(stack)) {
                otherItems++;
                continue;
            }
            if (firstSlot == -1) {
                firstSlot = i;
            } else if (secondSlot == -1) {
                secondSlot = i;
            } else {
                otherItems++;
            }
        }

        if (otherItems > 0 || firstSlot == -1 || secondSlot == -1) {
            return null;
        }

        int amount = pourAmount(inv.getStackInSlot(firstSlot), inv.getStackInSlot(secondSlot));
        if (amount > 0) {
            return new int[] {firstSlot, secondSlot, amount};
        }

        amount = pourAmount(inv.getStackInSlot(secondSlot), inv.getStackInSlot(firstSlot));
        if (amount > 0) {
            return new int[] {secondSlot, firstSlot, amount};
        }

        return null;
    }

    private static int pourAmount(ItemStack source, ItemStack destination) {
        IFluidHandlerItem sourceHandler = source.copy().getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        IFluidHandlerItem destHandler = destination.copy().getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (sourceHandler == null || destHandler == null) {
            return 0;
        }

        FluidStack available = sourceHandler.drain(Integer.MAX_VALUE, false);
        if (available == null || available.amount <= 0) {
            return 0;
        }

        return destHandler.fill(available, false);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return findPour(inv) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        int[] pour = findPour(inv);
        if (pour == null) {
            return ItemStack.EMPTY;
        }

        ItemStack destination = inv.getStackInSlot(pour[1]).copy();
        ItemStack sourceProbe = inv.getStackInSlot(pour[0]).copy();

        IFluidHandlerItem sourceHandler = sourceProbe.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        IFluidHandlerItem destHandler = destination.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (sourceHandler == null || destHandler == null) {
            return ItemStack.EMPTY;
        }

        FluidStack drained = sourceHandler.drain(pour[2], true);
        if (drained != null) {
            destHandler.fill(drained, true);
        }
        return destHandler.getContainer();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        int[] pour = findPour(inv);
        if (pour == null) {
            return remaining;
        }

        ItemStack sourceProbe = inv.getStackInSlot(pour[0]).copy();
        IFluidHandlerItem sourceHandler = sourceProbe.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (sourceHandler != null) {
            sourceHandler.drain(pour[2], true);
            remaining.set(pour[0], sourceHandler.getContainer());
        }
        return remaining;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}