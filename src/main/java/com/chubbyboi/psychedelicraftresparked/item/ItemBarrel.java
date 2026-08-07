package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBarrel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
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

public class ItemBarrel extends ItemBlock {

    public ItemBarrel(Block block) {
        super(block);
        setTranslationKey(block.getTranslationKey());
        setRegistryName(block.getRegistryName());
        setMaxStackSize(16);
        setHasSubtypes(true);
        setMaxDamage(0);
        ItemInit.ITEMS.add(this);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) {
            return;
        }
        for (BlockPlanks.EnumType type : BlockPlanks.EnumType.values()) {
            items.add(new ItemStack(this, 1, type.getMetadata()));
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey() + "." + BlockPlanks.EnumType.byMetadata(stack.getMetadata()).getName();
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        FluidStack fluidStack = getContainedFluidStack(stack);
        return fluidStack != null && fluidStack.amount < TileEntityBarrel.CAPACITY;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        FluidStack fluidStack = getContainedFluidStack(stack);
        return fluidStack != null ? 1.0 - ((double) fluidStack.amount / (double) TileEntityBarrel.CAPACITY) : 0.0;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return getContainedFluidStack(stack) != null ? 1 : 16;
    }

    @Nonnull
    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new FluidHandlerItemStack(stack, TileEntityBarrel.CAPACITY) {
            @Override
            public int fill(FluidStack resource, boolean doFill) {
                if (isSealed(container)) {
                    return 0;
                }
                return super.fill(resource, doFill);
            }

            @Override
            public FluidStack drain(FluidStack resource, boolean doDrain) {
                if (doDrain && isSealed(container)) {
                    return null;
                }
                return super.drain(resource, doDrain);
            }

            @Override
            public FluidStack drain(int maxDrain, boolean doDrain) {
                if (doDrain && isSealed(container)) {
                    return null;
                }
                return super.drain(maxDrain, doDrain);
            }

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
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStack fluidStack = getContainedFluidStack(stack);
        boolean sealed = isSealed(stack);
        if (fluidStack != null) {
            String key = getTranslationKey(stack) + (sealed ? ".sealed_full.name" : ".full.name");
            return I18n.translateToLocalFormatted(key, fluidStack.getFluid().getLocalizedName(fluidStack));
        }
        if (sealed) {
            return I18n.translateToLocal(getTranslationKey(stack) + ".sealed.name");
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
            tooltip.add(TextFormatting.GRAY + "" + fluidStack.amount + "mB/" + TileEntityBarrel.CAPACITY + "mB");
        }

        tooltip.add(TextFormatting.GRAY + I18n.translateToLocal(hasTap(stack)
            ? "psychedelicraftresparked.tooltip.barrel.tapped"
            : "psychedelicraftresparked.tooltip.barrel.untapped"));
    }

    @Nullable
    private FluidStack getContainedFluidStack(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) {
            return null;
        }
        FluidStack fluidStack = handler.drain(TileEntityBarrel.CAPACITY, false);
        return fluidStack != null && fluidStack.amount > 0 ? fluidStack : null;
    }

    // ==================== Sealed / tap state, carried on the stack's own NBT ====================

    public static boolean isSealed(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean("Sealed");
    }

    public static void setSealed(ItemStack stack, boolean sealed) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setBoolean("Sealed", sealed);
    }

    public static boolean hasTap(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean("HasTap");
    }

    public static void setHasTap(ItemStack stack, boolean hasTap) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setBoolean("HasTap", hasTap);
    }
}