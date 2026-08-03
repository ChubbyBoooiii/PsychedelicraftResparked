package com.chubbyboi.psychedelicraftresparked.recipes;

import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VatRecipes {

    private static final VatRecipes INSTANCE = new VatRecipes();

    public static VatRecipes getInstance() {
        return INSTANCE;
    }

    public static class Recipe {
        private final Fluid output;
        private final Fluid requiredFluid;
        private final int requiredAmount;
        private final Map<Item, Integer> ingredients = new LinkedHashMap<>();

        public Recipe(Fluid output, Fluid requiredFluid, int requiredAmount) {
            this.output = output;
            this.requiredFluid = requiredFluid;
            this.requiredAmount = requiredAmount;
        }

        public Recipe addIngredient(Item item, int count) {
            ingredients.merge(item, count, Integer::sum);
            return this;
        }

        public Fluid getOutput() {
            return output;
        }

        public Fluid getRequiredFluid() {
            return requiredFluid;
        }

        public int getRequiredAmount() {
            return requiredAmount;
        }

        public Map<Item, Integer> getIngredients() {
            return ingredients;
        }

        public boolean matches(FluidTank tank, NonNullList<ItemStack> slots) {
            if (tank.getFluid() == null || tank.getFluid().getFluid() != requiredFluid || tank.getFluidAmount() != requiredAmount) {
                return false;
            }

            Map<Item, Integer> have = new HashMap<>();
            for (ItemStack stack : slots) {
                if (!stack.isEmpty()) {
                    have.merge(stack.getItem(), stack.getCount(), Integer::sum);
                }
            }

            for (Map.Entry<Item, Integer> required : ingredients.entrySet()) {
                if (have.getOrDefault(required.getKey(), 0) < required.getValue()) {
                    return false;
                }
            }
            return true;
        }

        public void consumeIngredients(NonNullList<ItemStack> slots) {
            for (Map.Entry<Item, Integer> required : ingredients.entrySet()) {
                int remaining = required.getValue();
                for (int i = 0; i < slots.size() && remaining > 0; i++) {
                    ItemStack stack = slots.get(i);
                    if (!stack.isEmpty() && stack.getItem() == required.getKey()) {
                        int taken = Math.min(remaining, stack.getCount());
                        stack.shrink(taken);
                        remaining -= taken;
                    }
                }
            }
        }
    }

    private final List<Recipe> recipes = new ArrayList<>();

    private VatRecipes() {
        addRecipe(new Recipe(FluidInit.WHEAT, FluidRegistry.WATER, TileEntityVat.CAPACITY).addIngredient(Items.WHEAT, 7));
        addRecipe(new Recipe(FluidInit.COFFEE, FluidRegistry.WATER, 4000).addIngredient(ItemInit.COFFEE_BEANS, 7));
    }

    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
    }

    public Recipe findMatch(FluidTank tank, NonNullList<ItemStack> slots) {
        for (Recipe recipe : recipes) {
            if (recipe.matches(tank, slots)) {
                return recipe;
            }
        }
        return null;
    }

    public boolean isRawInput(Fluid fluid) {
        for (Recipe recipe : recipes) {
            if (recipe.getRequiredFluid() == fluid) {
                return true;
            }
        }
        return false;
    }
}