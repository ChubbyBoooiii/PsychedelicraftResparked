package com.chubbyboi.psychedelicraftresparked.util.compat.jei.containers;

import com.chubbyboi.psychedelicraftresparked.block.ContainerShape;
import com.chubbyboi.psychedelicraftresparked.block.PlacedContainerType;
import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import com.chubbyboi.psychedelicraftresparked.item.ItemBottle;
import com.chubbyboi.psychedelicraftresparked.item.ItemMolotovCocktail;
import com.chubbyboi.psychedelicraftresparked.recipes.RecipeConvertFluidContainer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConvertContainerRecipeWrapper implements IRecipeWrapper {

    private static final List<Fluid> REPRESENTATIVE_FLUIDS = buildRepresentativeFluids();

    private static List<Fluid> buildRepresentativeFluids() {
        List<Fluid> fluids = new ArrayList<>();
        fluids.add(FluidInit.COFFEE);
        fluids.add(FluidInit.CANNABIS_TEA);
        fluids.add(FluidInit.COCA_TEA);
        fluids.add(FluidInit.PEYOTE_JUICE);
        fluids.addAll(FluidInit.ALL_ALCOHOLS);
        return fluids;
    }

    private final List<List<ItemStack>> inputs;
    private final List<ItemStack> outputs;

    public ConvertContainerRecipeWrapper(RecipeConvertFluidContainer recipe) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(allVariants(recipe.getSourceItem()));
        for (Ingredient ingredient : recipe.getExtraIngredients()) {
            inputs.add(Arrays.asList(ingredient.getMatchingStacks()));
        }
        this.inputs = inputs;
        this.outputs = allVariants(recipe.getResultItem());
    }

    public static List<ItemStack> allVariants(Item item) {
        List<ItemStack> baseVariants = baseVariants(item);
        if (!new ItemStack(item).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return baseVariants;
        }

        List<ItemStack> variants = new ArrayList<>(baseVariants);
        for (ItemStack base : baseVariants) {
            for (Fluid fluid : REPRESENTATIVE_FLUIDS) {
                ItemStack filled = base.copy();
                filled.setCount(1);
                IFluidHandlerItem handler = filled.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (handler != null && handler.fill(new FluidStack(fluid, Integer.MAX_VALUE), true) > 0) {
                    variants.add(filled);
                }
            }
        }
        return variants;
    }

    private static List<ItemStack> baseVariants(Item item) {
        if (item instanceof ItemMolotovCocktail) {
            List<ItemStack> molotovs = new ArrayList<>();
            for (ContainerShape shape : PlacedContainerType.BOTTLE.getShapes()) {
                for (int meta = 0; meta <= ItemBottle.CLEAR_META; meta++) {
                    molotovs.add(PlacedContainerType.withShape(new ItemStack(item, 1, meta), shape.name));
                }
            }
            return molotovs;
        }
        NonNullList<ItemStack> variants = NonNullList.create();
        item.getSubItems(CreativeTabs.SEARCH, variants);
        if (!variants.isEmpty()) {
            return variants;
        }
        if (item.getHasSubtypes()) {
            List<ItemStack> metaVariants = new ArrayList<>();
            for (int meta = 0; meta < 16; meta++) {
                metaVariants.add(new ItemStack(item, 1, meta));
            }
            return metaVariants;
        }
        return Collections.singletonList(new ItemStack(item));
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }
}