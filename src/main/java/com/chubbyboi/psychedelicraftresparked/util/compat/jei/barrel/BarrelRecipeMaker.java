package com.chubbyboi.psychedelicraftresparked.util.compat.jei.barrel;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.init.FluidInit;

import java.util.ArrayList;
import java.util.List;

public final class BarrelRecipeMaker {

    private BarrelRecipeMaker() {
    }

    public static List<BarrelRecipeWrapper> getRecipes() {
        List<BarrelRecipeWrapper> recipes = new ArrayList<>();
        for (FluidAlcohol fluid : FluidInit.ALL_ALCOHOLS) {
            recipes.add(new BarrelRecipeWrapper(fluid, 0));
            recipes.add(new BarrelRecipeWrapper(fluid, 1));
        }
        return recipes;
    }
}