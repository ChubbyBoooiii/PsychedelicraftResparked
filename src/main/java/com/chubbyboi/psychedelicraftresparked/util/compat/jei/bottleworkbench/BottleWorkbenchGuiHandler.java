package com.chubbyboi.psychedelicraftresparked.util.compat.jei.bottleworkbench;

import com.chubbyboi.psychedelicraftresparked.gui.BottleWorkbenchGui;
import mezz.jei.api.gui.IAdvancedGuiHandler;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.List;

// Keeps JEI's item list off the mode tabs, which stick out past the GUI's width
public class BottleWorkbenchGuiHandler implements IAdvancedGuiHandler<BottleWorkbenchGui> {

    @Override
    public Class<BottleWorkbenchGui> getGuiContainerClass() {
        return BottleWorkbenchGui.class;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(BottleWorkbenchGui guiContainer) {
        return guiContainer.getExtraAreas();
    }
}