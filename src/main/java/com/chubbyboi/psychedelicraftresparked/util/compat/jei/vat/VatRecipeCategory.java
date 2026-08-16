package com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.PsychedelicraftRecipeCategory;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.PsychedelicraftRecipeCategoryUid;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class VatRecipeCategory extends PsychedelicraftRecipeCategory<VatRecipeWrapper> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/gui/gui_vat.png");

    static final int TANK_TEX_X = 59;
    static final int TANK_TEX_Y = 13;
    static final int TANK_WIDTH = 110;
    static final int TANK_HEIGHT = 59;

    static final int TANK_FILL_X = 1;
    static final int TANK_FILL_Y = 1;
    static final int TANK_FILL_WIDTH = 108;
    static final int TANK_FILL_HEIGHT = 57;

    static final int[][] INGREDIENT_SLOTS = {
        {20, 12}, {38, 12}, {56, 12}, {74, 12},
        {29, 32}, {47, 32}, {65, 32}
    };

    private static final int ARROW_TRACK_TEX_X = 21;
    private static final int ARROW_TRACK_TEX_Y = 14;
    private static final int ARROW_FILL_TEX_X = 176;
    private static final int ARROW_FILL_TEX_Y = 0;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;

    private static final int ARROW_GAP = 12;
    private static final int ARROW_X = TANK_WIDTH + ARROW_GAP;
    private static final int ARROW_Y = (TANK_HEIGHT - ARROW_HEIGHT) / 2;

    static final int TIME_TEXT_Y = ARROW_Y + ARROW_HEIGHT + 3;
    static final int ARROW_TIME_TEXT_CENTER_X = ARROW_X + ARROW_WIDTH / 2;

    private static final int OUTPUT_GAP = 12;
    private static final int OUTPUT_ICON_SIZE = 16;

    private final IDrawable tank;
    private final IDrawableStatic arrowTrack;
    private final IDrawableAnimated arrowFill;
    private final IDrawableStatic outputSlot;

    private final int outputSlotX;
    private final int outputSlotY;

    public VatRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper.createBlankDrawable(
            ARROW_X + ARROW_WIDTH + OUTPUT_GAP + guiHelper.getSlotDrawable().getWidth(),
            TANK_HEIGHT), "container.vat");
        this.tank = guiHelper.createDrawable(TEXTURE, TANK_TEX_X, TANK_TEX_Y, TANK_WIDTH, TANK_HEIGHT);
        this.arrowTrack = guiHelper.createDrawable(TEXTURE, ARROW_TRACK_TEX_X, ARROW_TRACK_TEX_Y, ARROW_WIDTH, ARROW_HEIGHT);
        this.arrowFill = guiHelper.drawableBuilder(TEXTURE, ARROW_FILL_TEX_X, ARROW_FILL_TEX_Y, ARROW_WIDTH, ARROW_HEIGHT)
            .buildAnimated(TileEntityVat.MIXING_TIME, IDrawableAnimated.StartDirection.LEFT, false);
        this.outputSlot = guiHelper.getSlotDrawable();

        this.outputSlotX = ARROW_X + ARROW_WIDTH + OUTPUT_GAP;
        this.outputSlotY = (TANK_HEIGHT - outputSlot.getHeight()) / 2;
    }

    @Override
    public String getUid() {
        return PsychedelicraftRecipeCategoryUid.VAT;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        tank.draw(minecraft, 0, 0);
        arrowTrack.draw(minecraft, ARROW_X, ARROW_Y);
        arrowFill.draw(minecraft, ARROW_X, ARROW_Y);
        outputSlot.draw(minecraft, outputSlotX, outputSlotY);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, VatRecipeWrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        List<List<ItemStack>> slots = wrapper.getIngredientSlots();
        for (int i = 0; i < slots.size() && i < INGREDIENT_SLOTS.length; i++) {
            int[] pos = INGREDIENT_SLOTS[i];
            itemStacks.init(i, true, pos[0] - 1, pos[1] - 1);
            itemStacks.set(i, slots.get(i));
        }

        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();

        if (slots.isEmpty()) {
            fluidStacks.init(1, true, TANK_FILL_X, TANK_FILL_Y, TANK_FILL_WIDTH, TANK_FILL_HEIGHT,
                wrapper.getInputCapacity(), false, null);
            fluidStacks.set(1, wrapper.getInputFluid());
        }

        if (wrapper.getOutputFluid() != null) {
            fluidStacks.init(0, false, outputSlotX + 1, outputSlotY + 1, OUTPUT_ICON_SIZE, OUTPUT_ICON_SIZE,
                wrapper.getOutputFluid().amount, false, null);
            fluidStacks.set(0, wrapper.getOutputFluid());
        } else {
            itemStacks.init(7, false, outputSlotX, outputSlotY);
            itemStacks.set(7, wrapper.getOutputItem());
        }
    }
}