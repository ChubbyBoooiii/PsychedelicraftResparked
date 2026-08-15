package com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import com.chubbyboi.psychedelicraftresparked.recipes.VatRecipes;

import java.util.ArrayList;
import java.util.List;

public final class VatRecipeMaker {

    private VatRecipeMaker() {
    }

    public static List<VatRecipeWrapper> getRecipes() {
        List<VatRecipeWrapper> recipes = new ArrayList<>();

        for (VatRecipes.Recipe recipe : VatRecipes.getInstance().getRecipes()) {
            recipes.add(VatRecipeWrapper.mixing(recipe));
        }

        for (FluidAlcohol fluid : FluidInit.ALL_ALCOHOLS) {
            FluidAlcohol.TickInfo tickInfo = fluid.getTickInfo();
            recipes.add(VatRecipeWrapper.fermenting(fluid, 0, tickInfo.ticksPerFermentation));
            recipes.add(VatRecipeWrapper.fermenting(fluid, 1, tickInfo.ticksPerFermentation));
            recipes.add(VatRecipeWrapper.souring(fluid, tickInfo.ticksUntilAcetification));
        }

        recipes.add(VatRecipeWrapper.hardening());

        return recipes;
    }
}