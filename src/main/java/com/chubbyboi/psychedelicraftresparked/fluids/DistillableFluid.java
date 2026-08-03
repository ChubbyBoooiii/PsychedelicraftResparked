package com.chubbyboi.psychedelicraftresparked.fluids;

import net.minecraftforge.fluids.FluidStack;

public interface DistillableFluid {

    int UNDISTILLABLE = -1;

    int distillationTime(FluidStack fluidStack);

    FluidStack distillStep(FluidStack fluidStack);
}