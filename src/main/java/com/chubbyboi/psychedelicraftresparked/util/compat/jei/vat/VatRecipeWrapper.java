package com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat;

import com.chubbyboi.psychedelicraftresparked.client.rendering.FluidGuiRenderer;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidSlurry;
import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import com.chubbyboi.psychedelicraftresparked.recipes.VatRecipes;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import com.chubbyboi.psychedelicraftresparked.util.TimeHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat.VatRecipeCategory.ARROW_TIME_TEXT_CENTER_X;
import static com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat.VatRecipeCategory.INGREDIENT_SLOTS;
import static com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat.VatRecipeCategory.TANK_FILL_HEIGHT;
import static com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat.VatRecipeCategory.TANK_FILL_WIDTH;
import static com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat.VatRecipeCategory.TANK_FILL_X;
import static com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat.VatRecipeCategory.TANK_FILL_Y;
import static com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat.VatRecipeCategory.TIME_TEXT_Y;

public class VatRecipeWrapper implements IRecipeWrapper {

    private final List<List<ItemStack>> ingredientSlots;
    private final FluidStack inputFluid;
    private final int inputCapacity;
    @Nullable
    private final FluidStack outputFluid;
    @Nullable
    private final ItemStack outputItem;
    private final int ticks;

    private VatRecipeWrapper(List<List<ItemStack>> ingredientSlots, FluidStack inputFluid, int inputCapacity,
                              @Nullable FluidStack outputFluid, @Nullable ItemStack outputItem, int ticks) {
        this.ingredientSlots = ingredientSlots;
        this.inputFluid = inputFluid;
        this.inputCapacity = inputCapacity;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.ticks = ticks;
    }

    public static VatRecipeWrapper mixing(VatRecipes.Recipe recipe) {
        List<List<ItemStack>> slots = new ArrayList<>();
        for (VatRecipes.IngredientEntry entry : recipe.getIngredients()) {
            List<ItemStack> alternatives = new ArrayList<>();
            for (ItemStack stack : entry.getIngredient().getMatchingStacks()) {
                ItemStack single = stack.copy();
                single.setCount(1);
                alternatives.add(single);
            }
            for (int i = 0; i < entry.getCount(); i++) {
                slots.add(alternatives);
            }
        }
        FluidStack input = new FluidStack(recipe.getRequiredFluid(), recipe.getRequiredAmount());
        FluidStack output = new FluidStack(recipe.getOutput(), recipe.getRequiredAmount());
        return new VatRecipeWrapper(slots, input, TileEntityVat.CAPACITY, output, null, TileEntityVat.MIXING_TIME);
    }

    public static VatRecipeWrapper fermenting(FluidAlcohol fluid, int fromFermentation, int ticks) {
        FluidStack input = stage(fluid, fromFermentation, false);
        FluidStack output = stage(fluid, fromFermentation + 1, false);
        return new VatRecipeWrapper(Collections.emptyList(), input, input.amount, output, null, ticks);
    }

    public static VatRecipeWrapper souring(FluidAlcohol fluid, int ticks) {
        FluidStack input = stage(fluid, FluidAlcohol.FERMENTATION_STEPS, false);
        FluidStack output = stage(fluid, FluidAlcohol.FERMENTATION_STEPS, true);
        return new VatRecipeWrapper(Collections.emptyList(), input, input.amount, output, null, ticks);
    }

    public static VatRecipeWrapper hardening() {
        FluidStack input = new FluidStack(FluidInit.SLURRY, FluidSlurry.FLUID_PER_DIRT);
        ItemStack output = new ItemStack(Blocks.DIRT, 1);
        return new VatRecipeWrapper(Collections.emptyList(), input, TileEntityVat.CAPACITY, null, output, FluidSlurry.HARDENING_TIME);
    }

    private static FluidStack stage(FluidAlcohol fluid, int fermentation, boolean vinegar) {
        FluidStack stack = new FluidStack(fluid, TileEntityVat.CAPACITY);
        if (fermentation != 0) {
            fluid.setFermentation(stack, fermentation);
        }
        if (vinegar) {
            fluid.setVinegar(stack, vinegar);
        }
        return stack;
    }

    public List<List<ItemStack>> getIngredientSlots() {
        return ingredientSlots;
    }

    public FluidStack getInputFluid() {
        return inputFluid;
    }

    public int getInputCapacity() {
        return inputCapacity;
    }

    @Nullable
    public FluidStack getOutputFluid() {
        return outputFluid;
    }

    @Nullable
    public ItemStack getOutputItem() {
        return outputItem;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, ingredientSlots);
        ingredients.setInput(VanillaTypes.FLUID, inputFluid);
        if (outputFluid != null) {
            ingredients.setOutput(VanillaTypes.FLUID, outputFluid);
        } else {
            ingredients.setOutput(VanillaTypes.ITEM, outputItem);
        }
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (!ingredientSlots.isEmpty()) {
            int fillHeight = (int) ((long) inputFluid.amount * TANK_FILL_HEIGHT / inputCapacity);
            fillHeight = Math.max(0, Math.min(TANK_FILL_HEIGHT, fillHeight));
            if (fillHeight > 0) {
                FluidGuiRenderer.drawTiledFluidRect(inputFluid, TANK_FILL_X, TANK_FILL_Y + TANK_FILL_HEIGHT - fillHeight,
                    TANK_FILL_WIDTH, fillHeight);
            }
        }

        String timeText = TimeHelper.formatTicksAsTime(ticks);
        int textX = ARROW_TIME_TEXT_CENTER_X - minecraft.fontRenderer.getStringWidth(timeText) / 2;
        minecraft.fontRenderer.drawString(timeText, textX, TIME_TEXT_Y, Color.GRAY.getRGB());
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (ingredientSlots.isEmpty()) {
            return Collections.emptyList();
        }
        if (mouseX < TANK_FILL_X || mouseX >= TANK_FILL_X + TANK_FILL_WIDTH
            || mouseY < TANK_FILL_Y || mouseY >= TANK_FILL_Y + TANK_FILL_HEIGHT) {
            return Collections.emptyList();
        }
        for (int[] pos : INGREDIENT_SLOTS) {
            if (mouseX >= pos[0] && mouseX < pos[0] + 16 && mouseY >= pos[1] && mouseY < pos[1] + 16) {
                return Collections.emptyList();
            }
        }
        String text = inputFluid.getFluid().getLocalizedName(inputFluid)
            + " (" + inputFluid.amount + "mB / " + TileEntityVat.CAPACITY + "mB)";
        return Collections.singletonList(text);
    }
}