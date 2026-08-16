package com.chubbyboi.psychedelicraftresparked.util.compat.jei.barrel;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.util.TimeHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

import java.awt.Color;

import static com.chubbyboi.psychedelicraftresparked.util.compat.jei.barrel.BarrelRecipeCategory.ARROW_TIME_TEXT_CENTER_X;
import static com.chubbyboi.psychedelicraftresparked.util.compat.jei.barrel.BarrelRecipeCategory.TIME_TEXT_Y;

public class BarrelRecipeWrapper implements IRecipeWrapper {

    private static final int REPRESENTATIVE_AMOUNT = 1000;

    private final FluidStack input;
    private final FluidStack output;
    private final int ticks;

    public BarrelRecipeWrapper(FluidAlcohol fluid, int distillation) {
        input = new FluidStack(fluid, REPRESENTATIVE_AMOUNT);
        fluid.setFermentation(input, FluidAlcohol.FERMENTATION_STEPS);
        if (distillation > 0) {
            fluid.setDistillation(input, distillation);
        }

        output = input.copy();
        fluid.setMaturation(output, 1);

        ticks = fluid.getTickInfo().ticksPerMaturation;
    }

    public FluidStack getInput() {
        return input;
    }

    public FluidStack getOutput() {
        return output;
    }

    public int getTicks() {
        return ticks;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, input);
        ingredients.setOutput(VanillaTypes.FLUID, output);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String timeText = TimeHelper.formatTicksAsTime(ticks);
        int textX = ARROW_TIME_TEXT_CENTER_X - minecraft.fontRenderer.getStringWidth(timeText) / 2;
        minecraft.fontRenderer.drawString(timeText, textX, TIME_TEXT_Y, Color.GRAY.getRGB());
    }
}