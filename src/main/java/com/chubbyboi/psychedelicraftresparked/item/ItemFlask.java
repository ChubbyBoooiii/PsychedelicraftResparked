package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityFlask;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemFlask extends ItemBlock {

    public ItemFlask(Block block) {
        super(block);
        setTranslationKey(block.getTranslationKey());
        setRegistryName(block.getRegistryName());
        setMaxStackSize(16);
        ItemInit.ITEMS.add(this);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return getContainedFluidStack(stack) != null ? 1 : 16;
    }

    @Nonnull
    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new FluidHandlerItemStack(stack, TileEntityFlask.CAPACITY) {
            @Override
            protected void setContainerToEmpty() {
                super.setContainerToEmpty();
                NBTTagCompound tag = container.getTagCompound();
                if (tag != null && tag.getKeySet().isEmpty()) {
                    container.setTagCompound(null);
                }
            }
        };
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        FluidStack fluidStack = getContainedFluidStack(stack);
        return fluidStack != null && fluidStack.amount < TileEntityFlask.CAPACITY;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        FluidStack fluidStack = getContainedFluidStack(stack);
        return fluidStack != null ? 1.0 - ((double) fluidStack.amount / (double) TileEntityFlask.CAPACITY) : 0.0;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStack fluidStack = getContainedFluidStack(stack);
        if (fluidStack != null) {
            return I18n.translateToLocalFormatted(getTranslationKey(stack) + ".full.name", fluidStack.getFluid().getLocalizedName(fluidStack));
        }
        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        FluidStack fluidStack = getContainedFluidStack(stack);
        if (fluidStack == null) {
            tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("psychedelicraftresparked.tooltip.fluid.empty"));
        } else {
            tooltip.add(TextFormatting.GRAY + "" + fluidStack.amount + "mB/" + TileEntityFlask.CAPACITY + "mB");
        }
    }

    @Nullable
    private FluidStack getContainedFluidStack(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) {
            return null;
        }
        FluidStack fluidStack = handler.drain(TileEntityFlask.CAPACITY, false);
        return fluidStack != null && fluidStack.amount > 0 ? fluidStack : null;
    }
}