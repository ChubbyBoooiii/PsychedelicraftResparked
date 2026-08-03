package com.chubbyboi.psychedelicraftresparked.fluids;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface FermentableFluid {

    int UNFERMENTABLE = -1;

    int fermentationTime(FluidStack fluidStack, boolean openContainer);

    ItemStack fermentStep(FluidStack fluidStack, boolean openContainer);
}