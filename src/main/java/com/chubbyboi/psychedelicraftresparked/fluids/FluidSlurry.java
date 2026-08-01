package com.chubbyboi.psychedelicraftresparked.fluids;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class FluidSlurry extends Fluid implements UntintedFluid {

    public FluidSlurry(String fluidName, ResourceLocation still, ResourceLocation flowing) {
        super(fluidName, still, flowing);
    }
}
