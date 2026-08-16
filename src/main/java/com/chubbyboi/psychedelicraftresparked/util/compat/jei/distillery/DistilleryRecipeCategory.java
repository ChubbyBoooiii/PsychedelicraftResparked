package com.chubbyboi.psychedelicraftresparked.util.compat.jei.distillery;

import com.chubbyboi.psychedelicraftresparked.util.compat.jei.PsychedelicraftRecipeCategory;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.PsychedelicraftRecipeCategoryUid;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class DistilleryRecipeCategory extends PsychedelicraftRecipeCategory<DistilleryRecipeWrapper> {

    private static final ResourceLocation VANILLA_TEXTURE = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
    private static final int ARROW_TRACK_TEX_X = 24;
    private static final int ARROW_TRACK_TEX_Y = 132;
    private static final int ARROW_TEX_X = 82;
    private static final int ARROW_TEX_Y = 128;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;
    private static final int ARROW_TICKS_PER_CYCLE = 200;

    static final int SLOT_SIZE = 18;
    private static final int GAP = 14;

    static final int INPUT_X = 0;
    static final int SLOT_Y = 0;
    static final int ARROW_X = SLOT_SIZE + GAP;
    private static final int ARROW_Y = (SLOT_SIZE - ARROW_HEIGHT) / 2;
    static final int DISTILLED_X = ARROW_X + ARROW_WIDTH + GAP;
    static final int SLURRY_X = DISTILLED_X + SLOT_SIZE + GAP;

    static final int TIME_TEXT_Y = SLOT_SIZE + 3;
    static final int ARROW_TIME_TEXT_CENTER_X = ARROW_X + ARROW_WIDTH / 2;

    private final IDrawableStatic inputSlot;
    private final IDrawableStatic distilledSlot;
    private final IDrawableStatic slurrySlot;
    private final IDrawableStatic arrowTrack;
    private final IDrawableAnimated arrow;

    public DistilleryRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper.createBlankDrawable(SLURRY_X + SLOT_SIZE, SLOT_SIZE + 12), "container.distillery");

        this.inputSlot = guiHelper.getSlotDrawable();
        this.distilledSlot = guiHelper.getSlotDrawable();
        this.slurrySlot = guiHelper.getSlotDrawable();
        this.arrowTrack = guiHelper.createDrawable(VANILLA_TEXTURE, ARROW_TRACK_TEX_X, ARROW_TRACK_TEX_Y, ARROW_WIDTH, ARROW_HEIGHT);
        this.arrow = guiHelper.drawableBuilder(VANILLA_TEXTURE, ARROW_TEX_X, ARROW_TEX_Y, ARROW_WIDTH, ARROW_HEIGHT)
            .buildAnimated(ARROW_TICKS_PER_CYCLE, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public String getUid() {
        return PsychedelicraftRecipeCategoryUid.DISTILLERY;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        inputSlot.draw(minecraft, INPUT_X, SLOT_Y);
        distilledSlot.draw(minecraft, DISTILLED_X, SLOT_Y);
        slurrySlot.draw(minecraft, SLURRY_X, SLOT_Y);
        arrowTrack.draw(minecraft, ARROW_X, ARROW_Y);
        arrow.draw(minecraft, ARROW_X, ARROW_Y);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, DistilleryRecipeWrapper wrapper, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        int iconSize = SLOT_SIZE - 2;

        fluidStacks.init(0, true, INPUT_X + 1, SLOT_Y + 1, iconSize, iconSize, wrapper.getWash().amount, false, null);
        fluidStacks.set(0, wrapper.getWash());

        fluidStacks.init(1, false, DISTILLED_X + 1, SLOT_Y + 1, iconSize, iconSize, wrapper.getWash().amount, false, null);
        fluidStacks.set(1, wrapper.getDistilled());

        fluidStacks.init(2, false, SLURRY_X + 1, SLOT_Y + 1, iconSize, iconSize, wrapper.getWash().amount, false, null);
        fluidStacks.set(2, wrapper.getSlurry());
    }
}