package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.fluids.DrinkableFluid;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.fluids.InjectableFluid;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemDrinkable extends net.minecraftforge.fluids.capability.ItemFluidContainer {

    public enum ConsumptionType {
        DRINK(EnumAction.DRINK),
        INJECT(EnumAction.BOW);

        public final EnumAction useAction;

        ConsumptionType(EnumAction useAction) {
            this.useAction = useAction;
        }
    }

    private static final int GULP_COOLDOWN_TICKS = 5;

    private final ConsumptionType consumptionType;
    private final int consumptionVolume;
    private final int useDuration;
    private SoundEvent finishSound;

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

    public ItemDrinkable setFinishSound(SoundEvent finishSound) {
        this.finishSound = finishSound;
        return this;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        FluidStack fluidStack = getContainedFluidStack(stack);
        return fluidStack != null && fluidStack.amount < capacity;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        FluidStack fluidStack = getContainedFluidStack(stack);
        return fluidStack != null ? 1.0 - ((double) fluidStack.amount / (double) capacity) : 0.0;
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

            @Override
            public boolean canFillFluidType(FluidStack fluid) {
                Fluid f = fluid.getFluid();
                return consumptionType == ConsumptionType.DRINK
                    ? f instanceof DrinkableFluid && ((DrinkableFluid) f).canDrink(fluid, null)
                    : f instanceof InjectableFluid && ((InjectableFluid) f).canInject(fluid, null);
            }
        };
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
            tooltip.add(TextFormatting.GRAY + "" + fluidStack.amount + "mB/" + capacity + "mB");
            FluidHelper.appendPotencyTooltip(tooltip, fluidStack);
        }
    }

    @Nullable
    private FluidStack getContainedFluidStack(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) {
            return null;
        }
        FluidStack fluidStack = handler.drain(capacity, false);
        return fluidStack != null && fluidStack.amount > 0 ? fluidStack : null;
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
            FluidStack drunk = consume(stack, entityLiving, true);

            if (drunk != null) {
                if (finishSound != null) {
                    worldIn.playSound(null, entityLiving.posX, entityLiving.posY, entityLiving.posZ, finishSound, SoundCategory.PLAYERS, 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
                }
                if (entityLiving instanceof EntityPlayer) {
                    ((EntityPlayer) entityLiving).getCooldownTracker().setCooldown(this, GULP_COOLDOWN_TICKS);
                }
            }
        }
        return stack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (playerIn.getCooldownTracker().hasCooldown(this)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

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

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (consumptionType == ConsumptionType.INJECT && target instanceof EntityPlayer
                && !target.world.isRemote && itemRand.nextFloat() < 0.5F) {
            FluidHelper.inject(stack, target, consumptionVolume, true);
        }
        return true;
    }
}