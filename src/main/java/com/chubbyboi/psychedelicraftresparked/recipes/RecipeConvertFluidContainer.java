package com.chubbyboi.psychedelicraftresparked.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
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

public class RecipeConvertFluidContainer extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private final Item sourceItem;
    private final Item resultItem;
    private final Ingredient[] extraIngredients;

    public RecipeConvertFluidContainer(Item sourceItem, Item resultItem, Ingredient... extraIngredients) {
        this.sourceItem = sourceItem;
        this.resultItem = resultItem;
        this.extraIngredients = extraIngredients;
    }

    private ItemStack findSource(InventoryCrafting inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == sourceItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        ItemStack source = findSource(inv);
        if (source.isEmpty()) {
            return false;
        }

        List<Ingredient> required = new ArrayList<>();
        for (Ingredient ingredient : extraIngredients) {
            required.add(ingredient);
        }

        boolean sourceSlotSkipped = false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack slot = inv.getStackInSlot(i);
            if (slot.isEmpty()) {
                continue;
            }
            if (!sourceSlotSkipped && slot == source) {
                sourceSlotSkipped = true;
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
        ItemStack source = findSource(inv);
        if (source.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(resultItem, 1, source.getMetadata());

        IFluidHandlerItem sourceHandler = source.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        IFluidHandlerItem resultHandler = result.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (sourceHandler != null && resultHandler != null) {
            FluidStack containedFluid = sourceHandler.drain(Integer.MAX_VALUE, false);
            if (containedFluid != null) {
                resultHandler.fill(containedFluid, true);
                result = resultHandler.getContainer();
            }
        }

        return result;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= extraIngredients.length + 1;
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