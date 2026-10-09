package com.chubbyboi.psychedelicraftresparked.util.compat.jei.bottleworkbench;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.block.ContainerShape;
import com.chubbyboi.psychedelicraftresparked.block.PlacedContainerType;
import com.chubbyboi.psychedelicraftresparked.recipes.BottleWorkbenchRecipes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BottleWorkbenchRecipeWrapper implements IRecipeWrapper {

    private final ContainerShape shape;
    private final boolean dyed;
    private final List<ItemStack> glass;
    private final List<ItemStack> dyes;
    private final List<ItemStack> bottles;

    private BottleWorkbenchRecipeWrapper(ContainerShape shape, boolean dyed, List<ItemStack> glass, List<ItemStack> dyes, List<ItemStack> bottles) {
        this.shape = shape;
        this.dyed = dyed;
        this.glass = glass;
        this.dyes = dyes;
        this.bottles = bottles;
    }

    public static BottleWorkbenchRecipeWrapper glassOnly(ContainerShape shape, List<ItemStack> allGlass) {
        List<ItemStack> bottles = new ArrayList<>();
        for (ItemStack glass : allGlass) {
            bottles.add(BottleWorkbenchRecipes.getResult(glass, ItemStack.EMPTY, shape));
        }
        return new BottleWorkbenchRecipeWrapper(shape, false, allGlass, Collections.emptyList(), bottles);
    }

    public static BottleWorkbenchRecipeWrapper withDye(ContainerShape shape, List<ItemStack> allGlass, List<ItemStack> allDyes) {
        ItemStack plainGlass = new ItemStack(Blocks.GLASS);
        List<ItemStack> bottles = new ArrayList<>();
        for (ItemStack dye : allDyes) {
            bottles.add(BottleWorkbenchRecipes.getResult(plainGlass, dye, shape));
        }
        return new BottleWorkbenchRecipeWrapper(shape, true, allGlass, allDyes, bottles);
    }

    public boolean isDyed() {
        return dyed;
    }

    public List<ItemStack> getLinkedInputs() {
        return dyed ? dyes : glass;
    }

    public List<ItemStack> getGlass() {
        return glass;
    }

    public List<ItemStack> getBottles() {
        return bottles;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, dyed ? Arrays.asList(glass, dyes) : Collections.singletonList(glass));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(bottles));
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String name = I18n.format(Tags.MOD_ID + ".shape." + shape.name);
        int x = (recipeWidth - minecraft.fontRenderer.getStringWidth(name)) / 2;
        minecraft.fontRenderer.drawString(name, x, BottleWorkbenchRecipeCategory.TEXT_Y, Color.GRAY.getRGB());
    }

    static boolean sameBottle(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata()
            && PlacedContainerType.BOTTLE.getShape(a) == PlacedContainerType.BOTTLE.getShape(b);
    }
}