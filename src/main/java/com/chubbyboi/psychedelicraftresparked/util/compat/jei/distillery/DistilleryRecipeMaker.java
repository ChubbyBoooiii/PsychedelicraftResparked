package com.chubbyboi.psychedelicraftresparked.util.compat.jei.distillery;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.init.FluidInit;

import java.util.ArrayList;
import java.util.List;

public final class DistilleryRecipeMaker {

    private DistilleryRecipeMaker() {
    }

    public static List<DistilleryRecipeWrapper> getRecipes() {
        List<DistilleryRecipeWrapper> recipes = new ArrayList<>();
        for (FluidAlcohol fluid : FluidInit.ALL_ALCOHOLS) {
            recipes.add(new DistilleryRecipeWrapper(fluid));
        }
        return recipes;
    }
}