package com.chubbyboi.psychedelicraftresparked.util.compat.jei.dryingtable;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.PsychedelicraftRecipeCategory;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.PsychedelicraftRecipeCategoryUid;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class DryingTableRecipeCategory extends PsychedelicraftRecipeCategory<DryingTableRecipeWrapper> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/gui/gui_drying_table.png");

    private static final int GRID_CONTENT_X = 29;
    private static final int GRID_CONTENT_Y = 16;
    private static final int GRID_CONTENT_WIDTH = 90;
    private static final int GRID_CONTENT_HEIGHT = 54;

    private static final int BACKGROUND_WIDTH = 116;
    private static final int BACKGROUND_HEIGHT = GRID_CONTENT_HEIGHT;

    private static final int GRID_ICON_X = 0;
    private static final int GRID_ICON_Y = 0;
    private static final int GRID_PITCH = 18;

    private static final int OUTPUT_SLOT_TEX_X = 119;
    private static final int OUTPUT_SLOT_TEX_Y = 30;
    private static final int OUTPUT_SLOT_SIZE = 26;
    private static final int OUTPUT_SLOT_LOCAL_X = 90;
    private static final int OUTPUT_SLOT_LOCAL_Y = 24;
    private static final int OUTPUT_ICON_X = OUTPUT_SLOT_LOCAL_X + 4;
    private static final int OUTPUT_ICON_Y = OUTPUT_SLOT_LOCAL_Y + 4;

    private static final int ARROW_FILL_X = 176;
    private static final int ARROW_FILL_Y = 42;
    private static final int ARROW_FILL_WIDTH = 25;
    private static final int ARROW_FILL_HEIGHT = 16;
    private static final int ARROW_FILL_LOCAL_X = 59;
    private static final int ARROW_FILL_LOCAL_Y = 19;
    private static final int ARROW_FILL_TICKS_PER_CYCLE = 200;

    private static final int SUN_X = 177;
    private static final int SUN_Y = 20;
    private static final int SUN_WIDTH = 21;
    private static final int SUN_HEIGHT = 22;
    private static final int SUN_LOCAL_X = 93;
    private static final int SUN_LOCAL_Y = 0;

    private final IDrawable gridContent;
    private final IDrawable outputSlot;
    private final IDrawableAnimated arrowFill;
    private final IDrawable sun;

    public DryingTableRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper.createBlankDrawable(BACKGROUND_WIDTH, BACKGROUND_HEIGHT), "container.drying_table");
        this.gridContent = guiHelper.createDrawable(TEXTURE, GRID_CONTENT_X, GRID_CONTENT_Y, GRID_CONTENT_WIDTH, GRID_CONTENT_HEIGHT);
        this.outputSlot = guiHelper.createDrawable(TEXTURE, OUTPUT_SLOT_TEX_X, OUTPUT_SLOT_TEX_Y, OUTPUT_SLOT_SIZE, OUTPUT_SLOT_SIZE);
        this.arrowFill = guiHelper.drawableBuilder(TEXTURE, ARROW_FILL_X, ARROW_FILL_Y, ARROW_FILL_WIDTH, ARROW_FILL_HEIGHT)
            .buildAnimated(ARROW_FILL_TICKS_PER_CYCLE, IDrawableAnimated.StartDirection.LEFT, false);
        this.sun = guiHelper.createDrawable(TEXTURE, SUN_X, SUN_Y, SUN_WIDTH, SUN_HEIGHT);
    }

    @Override
    public String getUid() {
        return PsychedelicraftRecipeCategoryUid.DRYING_TABLE;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        gridContent.draw(minecraft, 0, 0);
        outputSlot.draw(minecraft, OUTPUT_SLOT_LOCAL_X, OUTPUT_SLOT_LOCAL_Y);
        arrowFill.draw(minecraft, ARROW_FILL_LOCAL_X, ARROW_FILL_LOCAL_Y);
        sun.draw(minecraft, SUN_LOCAL_X, SUN_LOCAL_Y);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, DryingTableRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                itemStacks.init(index++, true, GRID_ICON_X + col * GRID_PITCH, GRID_ICON_Y + row * GRID_PITCH);
            }
        }
        itemStacks.init(index, false, OUTPUT_ICON_X, OUTPUT_ICON_Y);
        itemStacks.set(ingredients);
    }
}