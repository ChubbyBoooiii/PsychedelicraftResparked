package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemDrinkable extends net.minecraftforge.fluids.capability.ItemFluidContainer {

    public enum ConsumptionType {
        DRINK(EnumAction.DRINK),
        INJECT(EnumAction.BOW);

        public final EnumAction useAction;

        ConsumptionType(EnumAction useAction) {
            this.useAction = useAction;
        }
    }

    private final ConsumptionType consumptionType;
    private final int consumptionVolume;
    private final int useDuration;

    public ItemDrinkable(String name, int capacity, int consumptionVolume, int useDuration, ConsumptionType consumptionType) {
        super(capacity);
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);
        this.consumptionVolume = consumptionVolume;
        this.useDuration = useDuration;
        this.consumptionType = consumptionType;

        this.addPropertyOverride(new ResourceLocation("filled"), (stack, worldIn, entityIn) -> {
            IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            return handler != null && handler.drain(1, false) != null ? 1.0F : 0.0F;
        });

        ItemInit.ITEMS.add(this);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        return handler != null && handler.drain(1, false) != null ? 1 : 64;
    }

    @Nonnull
    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new FluidHandlerItemStack(stack, capacity) {
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
    public EnumAction getItemUseAction(ItemStack stack) {
        return consumptionType.useAction;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return useDuration;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (!worldIn.isRemote) {
            consume(stack, entityLiving, true);
        }
        return stack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (consume(stack, playerIn, false) != null) {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    private net.minecraftforge.fluids.FluidStack consume(ItemStack stack, EntityLivingBase entity, boolean doConsume) {
        return consumptionType == ConsumptionType.DRINK
            ? FluidHelper.drink(stack, entity, consumptionVolume, doConsume)
            : FluidHelper.inject(stack, entity, consumptionVolume, doConsume);
    }
}
