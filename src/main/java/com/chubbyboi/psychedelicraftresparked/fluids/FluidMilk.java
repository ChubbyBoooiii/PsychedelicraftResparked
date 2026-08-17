package com.chubbyboi.psychedelicraftresparked.fluids;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidMilk extends Fluid implements DrinkableFluid {

    public FluidMilk(String fluidName, ResourceLocation still, ResourceLocation flowing) {
        super(fluidName, still, flowing);
    }

    @Override
    public boolean canDrink(FluidStack fluidStack, EntityLivingBase entity) {
        return true;
    }

    @Override
    public void drink(FluidStack fluidStack, EntityLivingBase entity) {
        entity.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
    }
}