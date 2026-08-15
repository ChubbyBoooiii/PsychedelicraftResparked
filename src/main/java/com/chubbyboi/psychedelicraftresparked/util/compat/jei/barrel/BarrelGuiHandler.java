package com.chubbyboi.psychedelicraftresparked.util.compat.jei.barrel;

import com.chubbyboi.psychedelicraftresparked.gui.BarrelGui;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class BarrelGuiHandler implements IAdvancedGuiHandler<BarrelGui> {

    private static final int TANK_X = 60;
    private static final int TANK_Y = 14;
    private static final int TANK_WIDTH = 108;
    private static final int TANK_HEIGHT = 57;

    @Override
    public Class<BarrelGui> getGuiContainerClass() {
        return BarrelGui.class;
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(BarrelGui guiContainer, int mouseX, int mouseY) {
        int tankX = guiContainer.getGuiLeft() + TANK_X;
        int tankY = guiContainer.getGuiTop() + TANK_Y;
        if (mouseX < tankX || mouseX >= tankX + TANK_WIDTH || mouseY < tankY || mouseY >= tankY + TANK_HEIGHT) {
            return null;
        }

        FluidStack fluid = guiContainer.getTileEntity().getTank().getFluid();
        return fluid;
    }
}