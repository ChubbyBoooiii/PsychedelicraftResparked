package com.chubbyboi.psychedelicraftresparked.fluids;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fluids.FluidStack;


public interface DrinkableFluid {

    boolean canDrink(FluidStack fluidStack, EntityLivingBase entity);
    void drink(FluidStack fluidStack, EntityLivingBase entity);
}
