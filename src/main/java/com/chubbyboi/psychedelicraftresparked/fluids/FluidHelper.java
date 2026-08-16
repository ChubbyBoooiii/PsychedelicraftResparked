package com.chubbyboi.psychedelicraftresparked.fluids;

import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidHelper {

    public static final int BUCKET_VOLUME = 1000;
    public static final int FLUID_IO_SPEED_PER_TICK = 100;

    public static Fluid getBucketFluid(Item bucketItem) {
        if (bucketItem == Items.WATER_BUCKET) {
            return FluidRegistry.WATER;
        }
        if (bucketItem == Items.MILK_BUCKET) {
            return FluidInit.MILK;
        }
        return null;
    }

    public static Item getFilledBucket(Fluid fluid) {
        if (fluid == FluidRegistry.WATER) {
            return Items.WATER_BUCKET;
        }
        if (fluid == FluidInit.MILK) {
            return Items.MILK_BUCKET;
        }
        return null;
    }

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
                return getFlatTintColor(fluidStack);
            }
        }
        return 0xFFFFFF;
    }

    public static boolean hasFluid(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        return handler != null && handler.drain(1, false) != null;
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

    public static int getFlatTintColor(FluidStack fluidStack) {
        if (fluidStack == null) {
            return 0xFFFFFFFF;
        }
        if (fluidStack.getFluid() instanceof FluidAlcohol) {
            return ((FluidAlcohol) fluidStack.getFluid()).getFlatTintColor(fluidStack);
        }
        return getDisplayColor(fluidStack);
    }

    public static void appendPotencyTooltip(java.util.List<String> tooltip, FluidStack fluidStack) {
        if (fluidStack != null && fluidStack.getFluid() instanceof FluidAlcohol) {
            double potency = ((FluidAlcohol) fluidStack.getFluid()).getAlcoholContent(fluidStack);
            tooltip.add(net.minecraft.util.text.TextFormatting.GRAY + net.minecraft.util.text.translation.I18n.translateToLocalFormatted(
                "psychedelicraftresparked.tooltip.fluid.potency", String.format("%.1f", potency)));
        }
    }
}