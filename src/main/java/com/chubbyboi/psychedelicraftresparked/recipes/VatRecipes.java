package com.chubbyboi.psychedelicraftresparked.recipes;

import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.oredict.OreIngredient;

import java.util.ArrayList;
import java.util.List;

public class VatRecipes {

    private static final VatRecipes INSTANCE = new VatRecipes();

    public static VatRecipes getInstance() {
        return INSTANCE;
    }

    private static class AnyIngredient extends Ingredient {
        private final List<Ingredient> parts;

        AnyIngredient(List<Ingredient> parts) {
            super(0);
            this.parts = parts;
        }

        @Override
        public boolean apply(ItemStack stack) {
            if (stack == null) {
                return false;
            }
            for (Ingredient part : parts) {
                if (part.apply(stack)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public ItemStack[] getMatchingStacks() {
            List<ItemStack> combined = new ArrayList<>();
            for (Ingredient part : parts) {
                combined.addAll(java.util.Arrays.asList(part.getMatchingStacks()));
            }
            return combined.toArray(new ItemStack[0]);
        }
    }

    public static class IngredientEntry {
        private final String key;
        private final String[] defaultOreNames;
        private Ingredient ingredient;
        private final int count;

        IngredientEntry(String key, int count, String[] defaultOreNames) {
            this.key = key;
            this.count = count;
            this.defaultOreNames = defaultOreNames;
            this.ingredient = buildIngredient(defaultOreNames);
        }

        private static Ingredient buildIngredient(String[] oreNames) {
            List<Ingredient> parts = new ArrayList<>();
            for (String oreName : oreNames) {
                parts.add(new OreIngredient(oreName));
            }
            return new AnyIngredient(parts);
        }

        /** Replaces the matched oreDict tags entirely - does not merge with the defaults. */
        public void setOreNames(String... oreNames) {
            this.ingredient = buildIngredient(oreNames);
        }

        public String getKey() {
            return key;
        }

        public String[] getDefaultOreNames() {
            return defaultOreNames;
        }

        public Ingredient getIngredient() {
            return ingredient;
        }

        public int getCount() {
            return count;
        }
    }

    public static class Recipe {
        private final Fluid output;
        private final Fluid requiredFluid;
        private final int requiredAmount;
        private final List<IngredientEntry> ingredients = new ArrayList<>();

        public Recipe(Fluid output, Fluid requiredFluid, int requiredAmount) {
            this.output = output;
            this.requiredFluid = requiredFluid;
            this.requiredAmount = requiredAmount;
        }

        public Recipe addIngredient(String key, int count, String... oreNames) {
            ingredients.add(new IngredientEntry(key, count, oreNames));
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

        public List<IngredientEntry> getIngredients() {
            return ingredients;
        }

        public boolean matches(FluidTank tank, NonNullList<ItemStack> slots) {
            if (tank.getFluid() == null || tank.getFluid().getFluid() != requiredFluid || tank.getFluidAmount() != requiredAmount) {
                return false;
            }

            for (IngredientEntry entry : ingredients) {
                int have = 0;
                for (ItemStack stack : slots) {
                    if (!stack.isEmpty() && entry.getIngredient().apply(stack)) {
                        have += stack.getCount();
                    }
                }
                if (have < entry.getCount()) {
                    return false;
                }
            }
            return true;
        }

        public void consumeIngredients(NonNullList<ItemStack> slots) {
            for (IngredientEntry entry : ingredients) {
                int remaining = entry.getCount();
                for (int i = 0; i < slots.size() && remaining > 0; i++) {
                    ItemStack stack = slots.get(i);
                    if (!stack.isEmpty() && entry.getIngredient().apply(stack)) {
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
        addRecipe(new Recipe(FluidInit.COFFEE, FluidRegistry.WATER, 4000).addIngredient("coffeeBeans", 7, "cropCoffee"));
        addRecipe(new Recipe(FluidInit.CANNABIS_TEA, FluidRegistry.WATER, 4000).addIngredient("cannabisLeaf", 7, "leafCannabis"));
        addRecipe(new Recipe(FluidInit.COCA_TEA, FluidRegistry.WATER, 4000).addIngredient("cocaLeaf", 7, "leafCoca"));
        addRecipe(new Recipe(FluidInit.PEYOTE_JUICE, FluidRegistry.WATER, 4000).addIngredient("driedPeyote", 7, "peyoteDried"));
        addRecipe(new Recipe(FluidInit.WHEAT_HOP, FluidRegistry.WATER, TileEntityVat.CAPACITY)
            .addIngredient("wheat", 5, "cropWheat")
            .addIngredient("hops", 2, "cropHops"));
        addRecipe(new Recipe(FluidInit.WHEAT, FluidRegistry.WATER, TileEntityVat.CAPACITY).addIngredient("wheat", 7, "cropWheat"));
        addRecipe(new Recipe(FluidInit.CORN, FluidRegistry.WATER, TileEntityVat.CAPACITY).addIngredient("corn", 7, "cropCorn"));
        addRecipe(new Recipe(FluidInit.POTATO, FluidRegistry.WATER, TileEntityVat.CAPACITY).addIngredient("potato", 7, "cropPotato"));
        addRecipe(new Recipe(FluidInit.GRAPES, FluidRegistry.WATER, TileEntityVat.CAPACITY)
            .addIngredient("grapes", 7, "cropGrape", "foodGrapesPurple", "foodGrapesRed"));
        addRecipe(new Recipe(FluidInit.RICE, FluidRegistry.WATER, TileEntityVat.CAPACITY).addIngredient("rice", 7, "cropRice"));
        addRecipe(new Recipe(FluidInit.HONEY, FluidRegistry.WATER, TileEntityVat.CAPACITY).addIngredient("honey", 7, "dropHoney"));
        addRecipe(new Recipe(FluidInit.JUNIPER, FluidRegistry.WATER, TileEntityVat.CAPACITY)
            .addIngredient("juniperBerries", 3, "cropJuniperberry")
            .addIngredient("sugar", 1, "listAllsugar")
            .addIngredient("grapes", 2, "cropGrape", "foodGrapesPurple", "foodGrapesRed")
            .addIngredient("wheat", 1, "cropWheat"));
        addRecipe(new Recipe(FluidInit.SUGAR_CANE, FluidRegistry.WATER, TileEntityVat.CAPACITY).addIngredient("sugarCane", 7, "sugarcane"));
        addRecipe(new Recipe(FluidInit.APPLE, FluidRegistry.WATER, TileEntityVat.CAPACITY).addIngredient("apple", 7, "cropApple", "foodApple"));
        addRecipe(new Recipe(FluidInit.PINEAPPLE, FluidRegistry.WATER, TileEntityVat.CAPACITY).addIngredient("pineapple", 7, "cropPineapple"));
        addRecipe(new Recipe(FluidInit.BANANA, FluidRegistry.WATER, TileEntityVat.CAPACITY).addIngredient("banana", 7, "cropBanana"));
        addRecipe(new Recipe(FluidInit.MILK_ALCOHOL, FluidInit.MILK, TileEntityVat.CAPACITY));
    }

    public void addRecipe(Recipe recipe) {
        recipes.add(recipe);
    }

    public List<Recipe> getRecipes() {
        return recipes;
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