package com.chubbyboi.psychedelicraftresparked.tabs;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBarrel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class PsychedelicraftResparkedDrinksTab extends CreativeTabs {

    private static final ContainerDef[] CONTAINERS = {
        new ContainerDef(ItemInit.BARREL_ITEM, TileEntityBarrel.CAPACITY),
        new ContainerDef(ItemInit.WOODEN_MUG, 500),
        new ContainerDef(ItemInit.GLASS_CHALICE, 250),
        new ContainerDef(ItemInit.SHOT_GLASS, 40),
    };

    private static final FluidAlcohol[] ALCOHOLIC_DRINKS = {
        FluidInit.WHEAT_HOP,
        FluidInit.WHEAT,
        FluidInit.CORN,
        FluidInit.POTATO,
        FluidInit.GRAPES,
        FluidInit.RICE,
        FluidInit.HONEY,
        FluidInit.JUNIPER,
        FluidInit.SUGAR_CANE,
        FluidInit.APPLE,
        FluidInit.PINEAPPLE,
        FluidInit.BANANA,
        FluidInit.MILK_ALCOHOL,
    };

    private static final Fluid[] SIMPLE_DRINKS = {
        FluidInit.COFFEE,
        FluidInit.CANNABIS_TEA,
        FluidInit.COCA_TEA,
        FluidInit.PEYOTE_JUICE,
    };

    private static final int[][] ALCOHOL_STAGES = {
        {0, 0, 0},                                // Base Fluid
        {1, 0, 0},                                // Fermented once
        {FluidAlcohol.FERMENTATION_STEPS, 0, 0},  // Fermented twice
        {FluidAlcohol.FERMENTATION_STEPS, 0, 7},  // Matured 7 times
        {FluidAlcohol.FERMENTATION_STEPS, 2, 0},  // Distilled 2 times
        {FluidAlcohol.FERMENTATION_STEPS, 2, 14}, // Distilled 2 times, matured 14 times
    };

    public PsychedelicraftResparkedDrinksTab(String label) {
        super(label);
    }

    @Override
    public ItemStack createIcon() {
        return filledStack(ItemInit.WOODEN_MUG, 500, alcoholStack(FluidInit.WHEAT, FluidAlcohol.FERMENTATION_STEPS, 0, 0));
    }

    @Override
    public void displayAllRelevantItems(NonNullList<ItemStack> items) {
        for (Fluid fluid : SIMPLE_DRINKS) {
            for (ContainerDef container : CONTAINERS) {
                items.add(filledStack(container.item, container.capacity, new FluidStack(fluid, container.capacity)));
            }
        }

        for (FluidAlcohol fluid : ALCOHOLIC_DRINKS) {
            for (ContainerDef container : CONTAINERS) {
                for (int[] stage : ALCOHOL_STAGES) {
                    items.add(filledStack(container.item, container.capacity, alcoholStack(fluid, stage[0], stage[1], stage[2])));
                }
            }
        }
    }

    private static FluidStack alcoholStack(FluidAlcohol fluid, int fermentation, int distillation, int maturation) {
        FluidStack stack = new FluidStack((Fluid) fluid, 1);
        fluid.setFermentation(stack, fermentation);
        fluid.setDistillation(stack, distillation);
        fluid.setMaturation(stack, maturation);
        return stack;
    }

    private static ItemStack filledStack(Item item, int amount, FluidStack fluid) {
        ItemStack stack = new ItemStack(item);
        fluid.amount = amount;
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler != null) {
            handler.fill(fluid, true);
        }
        return stack;
    }

    private static final class ContainerDef {
        final Item item;
        final int capacity;

        ContainerDef(Item item, int capacity) {
            this.item = item;
            this.capacity = capacity;
        }
    }
}