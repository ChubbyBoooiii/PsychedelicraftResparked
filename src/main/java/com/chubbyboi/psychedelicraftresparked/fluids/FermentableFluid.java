package com.chubbyboi.psychedelicraftresparked.fluids;

import net.minecraftforge.fluids.FluidStack;

public interface FermentableFluid {

    int UNFERMENTABLE = -1;

    int fermentationTime(FluidStack fluidStack, boolean openContainer);
    void fermentStep(FluidStack fluidStack, boolean openContainer);
}