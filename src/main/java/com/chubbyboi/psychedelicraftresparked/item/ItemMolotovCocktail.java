package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.entities.EntityMolotovCocktail;
import com.chubbyboi.psychedelicraftresparked.fluids.ExplodingFluid;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMolotovCocktail extends ItemBottle {

    private static final float THROW_VELOCITY = 1.0F;

    public ItemMolotovCocktail(String name, int capacity) {
        super(name, capacity, 0, 7200);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        // Empty - Stops the empty coloured bottles
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 7200;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (!(entityLiving instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entityLiving;

        float strength = timeLeft / (float) getMaxItemUseDuration(stack);

        worldIn.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL,
            0.5F, 0.4F / (worldIn.rand.nextFloat() * 0.4F + 0.8F));

        if (!worldIn.isRemote) {
            EntityMolotovCocktail molotov = new EntityMolotovCocktail(worldIn, player);
            ItemStack thrown = stack.copy();
            thrown.setCount(1);
            molotov.setMolotovStack(thrown);
            molotov.shoot(player, player.rotationPitch, player.rotationYaw, -20.0F, THROW_VELOCITY * strength, 1.0F);
            worldIn.spawnEntity(molotov);
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStack fluidStack = getContainedFluidStack(stack);
        if (fluidStack == null) {
            return I18n.translateToLocal("item.molotov_cocktail.empty");
        }
        return I18n.translateToLocal("item.molotov_cocktail.quality." + getQuality(fluidStack));
    }

    private static final float EXPLOSION_QUALITY_WEIGHT = 1.6F;
    private static final float FIRE_QUALITY_WEIGHT = 1.2F;

    private int getQuality(FluidStack fluidStack) {
        if (!(fluidStack.getFluid() instanceof ExplodingFluid)) {
            return 0;
        }
        ExplodingFluid exploding = (ExplodingFluid) fluidStack.getFluid();
        float explosionPart = exploding.explosionStrength(fluidStack) * EXPLOSION_QUALITY_WEIGHT;
        float firePart = exploding.fireStrength(fluidStack) * FIRE_QUALITY_WEIGHT;
        return MathHelper.clamp(MathHelper.floor(firePart + explosionPart + 0.5F), 0, 7);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        FluidStack fluidStack = getContainedFluidStack(stack);
        if (fluidStack == null) {
            tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("psychedelicraftresparked.tooltip.fluid.empty"));
        } else {
            tooltip.add(TextFormatting.GRAY + fluidStack.getLocalizedName());
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
}