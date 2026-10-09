package com.chubbyboi.psychedelicraftresparked.util.compat.jei.bottleworkbench;

import com.chubbyboi.psychedelicraftresparked.block.PlacedContainerType;
import mezz.jei.api.ISubtypeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class BottleSubtypeInterpreter implements ISubtypeRegistry.ISubtypeInterpreter {

    @Override
    public String apply(ItemStack itemStack) {
        StringBuilder info = new StringBuilder();
        IFluidHandlerItem handler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler != null) {
            for (IFluidTankProperties tank : handler.getTankProperties()) {
                FluidStack contents = tank.getContents();
                info.append(contents != null && contents.getFluid() != null ? contents.getFluid().getName() : "empty").append(";");
            }
        }
        info.append("m=").append(itemStack.getMetadata());
        info.append(";shape=").append(PlacedContainerType.BOTTLE.getShape(itemStack).name);
        return info.toString();
    }
}