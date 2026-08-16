package com.chubbyboi.psychedelicraftresparked.util.compat.jei;

import com.chubbyboi.psychedelicraftresparked.Tags;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.resources.I18n;

public abstract class PsychedelicraftRecipeCategory<T extends IRecipeWrapper> implements IRecipeCategory<T> {

    private final IDrawable background;
    private final String title;

    protected PsychedelicraftRecipeCategory(IDrawable background, String titleKey) {
        this.background = background;
        this.title = I18n.format(titleKey);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getModName() {
        return Tags.MOD_NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }
}