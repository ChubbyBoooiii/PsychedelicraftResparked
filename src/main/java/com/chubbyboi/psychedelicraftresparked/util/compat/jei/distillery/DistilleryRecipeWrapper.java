package com.chubbyboi.psychedelicraftresparked.util.compat.jei.distillery;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import com.chubbyboi.psychedelicraftresparked.util.TimeHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

import java.awt.Color;
import java.util.Arrays;

import static com.chubbyboi.psychedelicraftresparked.util.compat.jei.distillery.DistilleryRecipeCategory.ARROW_TIME_TEXT_CENTER_X;
import static com.chubbyboi.psychedelicraftresparked.util.compat.jei.distillery.DistilleryRecipeCategory.TIME_TEXT_Y;

public class DistilleryRecipeWrapper implements IRecipeWrapper {

    private static final int REPRESENTATIVE_AMOUNT = 1000;

    private final FluidStack wash;
    private final FluidStack distilled;
    private final FluidStack slurry;
    private final int ticks;

    public DistilleryRecipeWrapper(FluidAlcohol fluid) {
        wash = new FluidStack(fluid, REPRESENTATIVE_AMOUNT);
        fluid.setFermentation(wash, FluidAlcohol.FERMENTATION_STEPS);

        int distilledAmount = REPRESENTATIVE_AMOUNT / 2;
        distilled = new FluidStack(fluid, distilledAmount);
        fluid.setFermentation(distilled, FluidAlcohol.FERMENTATION_STEPS);
        fluid.setDistillation(distilled, 1);

        slurry = new FluidStack(FluidInit.SLURRY, REPRESENTATIVE_AMOUNT - distilledAmount);

        ticks = fluid.getTickInfo().ticksPerDistillation;
    }

    public FluidStack getWash() {
        return wash;
    }

    public FluidStack getDistilled() {
        return distilled;
    }

    public FluidStack getSlurry() {
        return slurry;
    }

    public int getTicks() {
        return ticks;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, wash);
        ingredients.setOutputs(VanillaTypes.FLUID, Arrays.asList(distilled, slurry));
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String timeText = TimeHelper.formatTicksAsTime(ticks);
        int textX = ARROW_TIME_TEXT_CENTER_X - minecraft.fontRenderer.getStringWidth(timeText) / 2;
        minecraft.fontRenderer.drawString(timeText, textX, TIME_TEXT_Y, Color.GRAY.getRGB());
    }
}