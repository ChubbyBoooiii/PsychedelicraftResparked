package com.chubbyboi.psychedelicraftresparked.util.compat.jei.vat;

import com.chubbyboi.psychedelicraftresparked.gui.VatGui;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class VatGuiHandler implements IAdvancedGuiHandler<VatGui> {

    private static final int TANK_X = 60;
    private static final int TANK_Y = 14;
    private static final int TANK_WIDTH = 108;
    private static final int TANK_HEIGHT = 57;

    @Override
    public Class<VatGui> getGuiContainerClass() {
        return VatGui.class;
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(VatGui guiContainer, int mouseX, int mouseY) {
        int tankX = guiContainer.getGuiLeft() + TANK_X;
        int tankY = guiContainer.getGuiTop() + TANK_Y;
        if (mouseX < tankX || mouseX >= tankX + TANK_WIDTH || mouseY < tankY || mouseY >= tankY + TANK_HEIGHT) {
            return null;
        }
        if (guiContainer.isMouseOverVisibleIngredientSlot(mouseX, mouseY)) {
            return null;
        }

        FluidStack fluid = guiContainer.getTileEntity().getTank().getFluid();
        return fluid;
    }
}