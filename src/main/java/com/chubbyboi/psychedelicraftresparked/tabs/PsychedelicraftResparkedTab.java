package com.chubbyboi.psychedelicraftresparked.tabs;

import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class PsychedelicraftResparkedTab extends CreativeTabs {

    public PsychedelicraftResparkedTab(String label) {
        super("psychedelicrafResparkedTab");
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(ItemInit.CANNABIS_LEAF, 1);
    }

    @Override
    public void displayAllRelevantItems(NonNullList<ItemStack> items) {
        super.displayAllRelevantItems(items);
        insertFilledAfter(items, ItemInit.COCAINE_POWDER, ItemInit.SYRINGE, FluidInit.COCAINE_FLUID, 10);
        insertFilledAfter(items, ItemInit.COFFEE_BEANS, ItemInit.SYRINGE, FluidInit.CAFFEINE_FLUID, 10);
    }

    private static void insertFilledAfter(NonNullList<ItemStack> items, Item afterItem, Item containerItem, net.minecraftforge.fluids.Fluid fluid, int amount) {
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItem() == afterItem) {
                index = i;
            }
        }
        if (index < 0) {
            return;
        }

        ItemStack filled = new ItemStack(containerItem);
        IFluidHandlerItem handler = filled.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler != null) {
            handler.fill(new FluidStack(fluid, amount), true);
        }

        items.add(index + 1, filled);
    }
}
