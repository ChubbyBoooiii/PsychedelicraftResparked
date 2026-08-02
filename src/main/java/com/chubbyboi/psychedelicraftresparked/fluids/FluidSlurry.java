package com.chubbyboi.psychedelicraftresparked.fluids;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidSlurry extends Fluid implements UntintedFluid, FermentableFluid {

    public static final int FLUID_PER_DIRT = FluidHelper.BUCKET_VOLUME * 4;
    public static final int HARDENING_TIME = 400; //36000

    public FluidSlurry(String fluidName, ResourceLocation still, ResourceLocation flowing) {
        super(fluidName, still, flowing);
    }

    @Override
    public int fermentationTime(FluidStack stack, boolean openContainer) {
        return stack.amount >= FLUID_PER_DIRT ? HARDENING_TIME : UNFERMENTABLE;
    }

    @Override
    public ItemStack fermentStep(FluidStack stack, boolean openContainer) {
        return new ItemStack(Blocks.DIRT, stack.amount / FLUID_PER_DIRT); // Round down
    }
}