package com.chubbyboi.psychedelicraftresparked.util.compat.jei.bottleworkbench;

import com.chubbyboi.psychedelicraftresparked.block.ContainerShape;
import com.chubbyboi.psychedelicraftresparked.recipes.BottleWorkbenchRecipes;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public final class BottleWorkbenchRecipeMaker {

    private BottleWorkbenchRecipeMaker() {
    }

    public static List<BottleWorkbenchRecipeWrapper> getRecipes() {
        List<ItemStack> glass = new ArrayList<>();
        for (ItemStack stack : expand(OreDictionary.getOres("blockGlass"))) {
            if (BottleWorkbenchRecipes.isGlass(stack)) {
                glass.add(stack);
            }
        }
        List<ItemStack> dyes = new ArrayList<>();
        for (ItemStack stack : expand(OreDictionary.getOres("dye"))) {
            if (BottleWorkbenchRecipes.isDye(stack)) {
                dyes.add(stack);
            }
        }

        List<BottleWorkbenchRecipeWrapper> recipes = new ArrayList<>();
        if (glass.isEmpty()) {
            return recipes;
        }
        for (ContainerShape shape : BottleWorkbenchRecipes.getShapes()) {
            recipes.add(BottleWorkbenchRecipeWrapper.glassOnly(shape, glass));
            if (!dyes.isEmpty()) {
                recipes.add(BottleWorkbenchRecipeWrapper.withDye(shape, glass, dyes));
            }
        }
        return recipes;
    }

    private static List<ItemStack> expand(List<ItemStack> ores) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack ore : ores) {
            if (ore.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                NonNullList<ItemStack> subItems = NonNullList.create();
                ore.getItem().getSubItems(CreativeTabs.SEARCH, subItems);
                for (ItemStack sub : subItems) {
                    addUnique(stacks, sub);
                }
            } else {
                addUnique(stacks, ore);
            }
        }
        return stacks;
    }

    private static void addUnique(List<ItemStack> stacks, ItemStack stack) {
        for (ItemStack existing : stacks) {
            if (ItemStack.areItemStacksEqual(existing, stack)) {
                return;
            }
        }
        ItemStack single = stack.copy();
        single.setCount(1);
        stacks.add(single);
    }
}