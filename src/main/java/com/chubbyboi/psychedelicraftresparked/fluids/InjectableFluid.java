package com.chubbyboi.psychedelicraftresparked.fluids;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fluids.FluidStack;


public interface InjectableFluid {

    boolean canInject(FluidStack fluidStack, EntityLivingBase entity);
    void inject(FluidStack fluidStack, EntityLivingBase entity);
}
