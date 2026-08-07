package com.chubbyboi.psychedelicraftresparked.fluids;

import net.minecraftforge.fluids.FluidStack;

public interface ExplodingFluid {

    float fireStrength(FluidStack fluidStack);

    float explosionStrength(FluidStack fluidStack);
}