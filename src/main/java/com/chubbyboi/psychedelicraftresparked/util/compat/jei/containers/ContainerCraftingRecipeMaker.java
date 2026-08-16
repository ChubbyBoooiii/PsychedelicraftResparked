package com.chubbyboi.psychedelicraftresparked.util.compat.jei.containers;

import com.chubbyboi.psychedelicraftresparked.recipes.RecipeConvertFluidContainer;
import com.chubbyboi.psychedelicraftresparked.recipes.RecipeFillContainer;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public final class ContainerCraftingRecipeMaker {

    private ContainerCraftingRecipeMaker() {
    }

    public static List<IRecipeWrapper> getRecipes() {
        List<IRecipeWrapper> recipes = new ArrayList<>();
        for (IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
            if (recipe instanceof RecipeFillContainer) {
                FillContainerRecipeWrapper wrapper = FillContainerRecipeWrapper.create((RecipeFillContainer) recipe);
                if (wrapper != null) {
                    recipes.add(wrapper);
                }
            } else if (recipe instanceof RecipeConvertFluidContainer) {
                recipes.add(new ConvertContainerRecipeWrapper((RecipeConvertFluidContainer) recipe));
            }
        }
        return recipes;
    }
}