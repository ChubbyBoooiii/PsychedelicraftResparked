package com.chubbyboi.psychedelicraftresparked.util.compat.jei.dryingtable;

import com.chubbyboi.psychedelicraftresparked.recipes.DryingTableRecipes;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DryingTableRecipeMaker {

    private DryingTableRecipeMaker() {
    }

    public static List<DryingTableRecipeWrapper> getRecipes() {
        List<DryingTableRecipeWrapper> recipes = new ArrayList<>();
        for (Map.Entry<ItemStack, ItemStack> entry : DryingTableRecipes.getInstance().getDryingList().entrySet()) {
            recipes.add(new DryingTableRecipeWrapper(entry.getKey(), entry.getValue()));
        }
        return recipes;
    }
}