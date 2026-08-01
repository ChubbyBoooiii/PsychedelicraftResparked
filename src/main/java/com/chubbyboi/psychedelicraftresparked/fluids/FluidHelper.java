package com.chubbyboi.psychedelicraftresparked.fluids;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidHelper {

    public static final int BUCKET_VOLUME = 1000;
    public static final int FLUID_IO_SPEED_PER_TICK = 100;

    public static FluidStack drink(ItemStack stack, EntityLivingBase entity, int maxDrunk, boolean doDrink) {
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) {
            return null;
        }

        FluidStack wouldDrain = handler.drain(maxDrunk, false);
        if (wouldDrain != null && wouldDrain.amount > 0) {
            Fluid fluid = wouldDrain.getFluid();
            if (fluid instanceof DrinkableFluid && ((DrinkableFluid) fluid).canDrink(wouldDrain, entity)) {
                FluidStack drained = handler.drain(maxDrunk, doDrink);
                if (doDrink && drained != null) {
                    ((DrinkableFluid) fluid).drink(drained, entity);
                }
                return drained;
            }
        }

        return null;
    }

    public static FluidStack inject(ItemStack stack, EntityLivingBase entity, int maxInjected, boolean doInject) {
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) {
            return null;
        }

        FluidStack wouldDrain = handler.drain(maxInjected, false);
        if (wouldDrain != null && wouldDrain.amount > 0) {
            Fluid fluid = wouldDrain.getFluid();
            if (fluid instanceof InjectableFluid && ((InjectableFluid) fluid).canInject(wouldDrain, entity)) {
                FluidStack drained = handler.drain(maxInjected, doInject);
                if (doInject && drained != null) {
                    ((InjectableFluid) fluid).inject(drained, entity);
                }
                return drained;
            }
        }

        return null;
    }

    public static int getFluidColor(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler != null) {
            FluidStack fluidStack = handler.drain(Integer.MAX_VALUE, false);
            if (fluidStack != null) {
                return getDisplayColor(fluidStack);
            }
        }
        return 0xFFFFFF;
    }

    public static int getDisplayColor(FluidStack fluidStack) {
        if (fluidStack == null) {
            return 0xFFFFFFFF;
        }
        int color = fluidStack.getFluid().getColor(fluidStack);
        if (color == 0xFFFFFFFF && fluidStack.getFluid() == FluidRegistry.WATER) {
            return 0xFF3F76E4;
        }
        return color;
    }

    public static int getWorldRenderColor(FluidStack fluidStack) {
        if (fluidStack != null && fluidStack.getFluid() instanceof UntintedFluid) {
            return 0xFFFFFFFF;
        }
        return getDisplayColor(fluidStack);
    }
}