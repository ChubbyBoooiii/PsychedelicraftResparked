package com.chubbyboi.psychedelicraftresparked.util.compat.jei.containers;

import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.recipes.RecipeFillContainer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FillContainerRecipeWrapper implements IRecipeWrapper {

    private final List<List<ItemStack>> inputs;
    private final ItemStack output;

    private FillContainerRecipeWrapper(List<List<ItemStack>> inputs, ItemStack output) {
        this.inputs = inputs;
        this.output = output;
    }

    @Nullable
    public static FillContainerRecipeWrapper create(RecipeFillContainer recipe) {
        FluidStack fill = recipe.getFill();
        ItemStack container = findRepresentativeContainer(fill);
        if (container.isEmpty()) {
            return null;
        }

        ItemStack output = container.copy();
        IFluidHandlerItem handler = output.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null || handler.fill(fill, true) < fill.amount) {
            return null;
        }
        output = handler.getContainer();

        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(Collections.singletonList(container));
        for (Ingredient ingredient : recipe.getExtraIngredients()) {
            inputs.add(Arrays.asList(ingredient.getMatchingStacks()));
        }

        return new FillContainerRecipeWrapper(inputs, output);
    }

    private static ItemStack findRepresentativeContainer(FluidStack fill) {
        ItemStack best = ItemStack.EMPTY;
        int bestCapacity = Integer.MAX_VALUE;
        for (Item item : ItemInit.ITEMS) {
            ItemStack candidate = new ItemStack(item);
            IFluidHandlerItem handler = candidate.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (handler == null) {
                continue;
            }
            int capacity = handler.fill(new FluidStack(fill.getFluid(), Integer.MAX_VALUE), false);
            if (capacity >= fill.amount && capacity < bestCapacity) {
                best = candidate;
                bestCapacity = capacity;
            }
        }
        return best;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, output);
    }
}