package com.chubbyboi.psychedelicraftresparked.util.compat.jei.bottleworkbench;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.recipes.BottleWorkbenchRecipes;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.PsychedelicraftRecipeCategory;
import com.chubbyboi.psychedelicraftresparked.util.compat.jei.PsychedelicraftRecipeCategoryUid;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class BottleWorkbenchRecipeCategory extends PsychedelicraftRecipeCategory<BottleWorkbenchRecipeWrapper> {

    private static final ResourceLocation VANILLA_TEXTURE = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
    private static final int ARROW_TRACK_TEX_X = 24;
    private static final int ARROW_TRACK_TEX_Y = 132;
    private static final int ARROW_TEX_X = 82;
    private static final int ARROW_TEX_Y = 128;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;
    private static final int ARROW_TICKS_PER_CYCLE = 200;

    private static final int SLOT_SIZE = 18;
    private static final int GAP = 8;
    private static final int GLASS_X = 0;
    private static final int DYE_X = SLOT_SIZE + 2;
    private static final int ARROW_X = DYE_X + SLOT_SIZE + GAP;
    private static final int ARROW_Y = (SLOT_SIZE - ARROW_HEIGHT) / 2;
    private static final int OUTPUT_X = ARROW_X + ARROW_WIDTH + GAP;
    private static final int WIDTH = OUTPUT_X + SLOT_SIZE;
    static final int TEXT_Y = SLOT_SIZE + 3;

    private static final int GLASS_SLOT = 0;
    private static final int DYE_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;

    private final IDrawableStatic slot;
    private final IDrawableStatic arrowTrack;
    private final IDrawableAnimated arrow;

    public BottleWorkbenchRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper.createBlankDrawable(WIDTH, SLOT_SIZE + 12), "container.bottle_workbench");
        this.slot = guiHelper.getSlotDrawable();
        this.arrowTrack = guiHelper.createDrawable(VANILLA_TEXTURE, ARROW_TRACK_TEX_X, ARROW_TRACK_TEX_Y, ARROW_WIDTH, ARROW_HEIGHT);
        this.arrow = guiHelper.drawableBuilder(VANILLA_TEXTURE, ARROW_TEX_X, ARROW_TEX_Y, ARROW_WIDTH, ARROW_HEIGHT)
            .buildAnimated(ARROW_TICKS_PER_CYCLE, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public String getUid() {
        return PsychedelicraftRecipeCategoryUid.BOTTLE_WORKBENCH;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        slot.draw(minecraft, GLASS_X, 0);
        slot.draw(minecraft, DYE_X, 0);
        slot.draw(minecraft, OUTPUT_X, 0);
        arrowTrack.draw(minecraft, ARROW_X, ARROW_Y);
        arrow.draw(minecraft, ARROW_X, ARROW_Y);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, BottleWorkbenchRecipeWrapper wrapper, IIngredients ingredients) {
        List<ItemStack> glass = wrapper.getGlass();
        List<ItemStack> linked = wrapper.getLinkedInputs();
        List<ItemStack> bottles = wrapper.getBottles();

        IFocus<?> focus = recipeLayout.getFocus();
        if (focus != null && focus.getValue() instanceof ItemStack) {
            ItemStack focused = (ItemStack) focus.getValue();
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < bottles.size(); i++) {
                boolean match = focus.getMode() == IFocus.Mode.OUTPUT
                    ? BottleWorkbenchRecipeWrapper.sameBottle(bottles.get(i), focused)
                    : ItemStack.areItemsEqual(linked.get(i), focused);
                if (match) {
                    indices.add(i);
                }
            }
            if (!indices.isEmpty()) {
                linked = pick(linked, indices);
                bottles = pick(bottles, indices);
                if (!wrapper.isDyed()) {
                    glass = linked;
                }
            } else if (wrapper.isDyed() && focus.getMode() == IFocus.Mode.INPUT && BottleWorkbenchRecipes.isGlass(focused)) {
                // Looked up a glass block: show just that glass, dyes and bottles still cycling together.
                List<ItemStack> single = new ArrayList<>();
                single.add(focused);
                glass = single;
            }
        }

        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
        stacks.init(GLASS_SLOT, true, GLASS_X, 0);
        stacks.set(GLASS_SLOT, glass);
        if (wrapper.isDyed()) {
            stacks.init(DYE_SLOT, true, DYE_X, 0);
            stacks.set(DYE_SLOT, linked);
        }
        stacks.init(OUTPUT_SLOT, false, OUTPUT_X, 0);
        stacks.set(OUTPUT_SLOT, bottles);

        if (wrapper.isDyed()) {
            stacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
                if (slotIndex == GLASS_SLOT) {
                    tooltip.add(TextFormatting.GRAY + I18n.format(Tags.MOD_ID + ".jei.bottle_workbench.any_glass"));
                }
            });
        }
    }

    private static List<ItemStack> pick(List<ItemStack> list, List<Integer> indices) {
        List<ItemStack> picked = new ArrayList<>();
        for (int index : indices) {
            picked.add(list.get(index));
        }
        return picked;
    }
}