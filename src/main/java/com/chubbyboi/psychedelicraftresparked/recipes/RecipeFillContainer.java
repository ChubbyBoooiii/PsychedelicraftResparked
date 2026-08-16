package com.chubbyboi.psychedelicraftresparked.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.ArrayList;
import java.util.List;

public class RecipeFillContainer extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private final FluidStack fill;
    private final Ingredient[] ingredients;

    public RecipeFillContainer(FluidStack fill, Ingredient... ingredients) {
        this.fill = fill;
        this.ingredients = ingredients;
    }

    public FluidStack getFill() {
        return fill;
    }

    public Ingredient[] getExtraIngredients() {
        return ingredients;
    }

    private ItemStack findContainer(InventoryCrafting inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack probe = stack.copy();
                probe.setCount(1);
                IFluidHandlerItem handler = probe.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (handler != null && handler.fill(fill, false) >= fill.amount) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        ItemStack container = findContainer(inv);
        if (container.isEmpty()) {
            return false;
        }

        List<Ingredient> required = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            required.add(ingredient);
        }

        boolean containerSlotSkipped = false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack slot = inv.getStackInSlot(i);
            if (slot.isEmpty()) {
                continue;
            }
            if (!containerSlotSkipped && slot == container) {
                containerSlotSkipped = true;
                continue;
            }

            boolean matched = false;
            for (int j = 0; j < required.size(); j++) {
                if (required.get(j).apply(slot)) {
                    required.remove(j);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }

        return required.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack container = findContainer(inv);
        if (container.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = container.copy();
        result.setCount(1);
        IFluidHandlerItem handler = result.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler != null) {
            handler.fill(fill, true);
        }
        return result;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= ingredients.length + 1;
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