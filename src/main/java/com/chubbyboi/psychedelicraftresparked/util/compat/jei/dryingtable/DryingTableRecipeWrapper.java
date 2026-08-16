package com.chubbyboi.psychedelicraftresparked.util.compat.jei.dryingtable;

import com.chubbyboi.psychedelicraftresparked.recipes.DryingTableRecipes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class DryingTableRecipeWrapper implements IRecipeWrapper {

    private static final int GRID_SLOTS = 9;

    private static final int EXPERIENCE_TEXT_CENTER_X = 71;
    private static final int EXPERIENCE_TEXT_BOTTOM_MARGIN = 10;

    private final ItemStack input;
    private final ItemStack output;
    private final float experience;

    public DryingTableRecipeWrapper(ItemStack input, ItemStack output) {
        this.input = input;
        this.output = output;
        this.experience = DryingTableRecipes.getInstance().getDryingExperience(output);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < GRID_SLOTS; i++) {
            inputs.add(input);
        }
        ingredients.setInputs(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, output);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (experience > 0) {
            String text = I18n.format("jei.drying_table.experience", experience);
            int x = EXPERIENCE_TEXT_CENTER_X - minecraft.fontRenderer.getStringWidth(text) / 2;
            int y = recipeHeight - EXPERIENCE_TEXT_BOTTOM_MARGIN;
            minecraft.fontRenderer.drawString(text, x, y, Color.GRAY.getRGB());
        }
    }
}