package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.drug.DrugInfluence;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ItemSmokable extends PsychItem {

    protected final List<DrugInfluence> drugInfluences = new ArrayList<>();

    protected int smokeDuration = 32;
    protected float[] smokeColor = {1.0f, 1.0f, 1.0f}; // Default white

    public ItemSmokable(String name) {
        super(name);
        this.setMaxStackSize(16);

        this.addPropertyOverride(new ResourceLocation("smoking"), (stack, worldIn, entityIn) -> {
            return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
        });
    }

    public ItemSmokable setUseStages(int useStages) {
        if (useStages > 1) {
            this.setMaxDamage(useStages - 1);
            this.setMaxStackSize(1);
        }
        return this;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public ItemSmokable addDrugInfluence(String drugType, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence) {
        drugInfluences.add(new DrugInfluence(drugType, delay, influenceSpeed, influenceSpeedPlus, maxInfluence));
        return this;
    }

    public ItemSmokable setSmokeDuration(int duration) {
        this.smokeDuration = duration;
        return this;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;

            // Validate drug parameters are set
            if (drugInfluences.isEmpty()) {
                throw new IllegalStateException("ItemSmokable '" + this.getRegistryName() + "' is missing drug parameters!");
            }

            IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);

            // Apply the dose
            if (!worldIn.isRemote && props instanceof DrugProperties) {
                for (DrugInfluence influence : drugInfluences) {
                    ((DrugProperties) props).addInfluence(influence.clone());
                }
            }

            if (props instanceof DrugProperties) {
                ((DrugProperties) props).startBreathingSmoke(10 + worldIn.rand.nextInt(10), smokeColor);
            }

            if (!player.capabilities.isCreativeMode) {
                if (stack.isItemStackDamageable()) {
                    stack.damageItem(1, player);
                } else {
                    stack.shrink(1);
                }
            }
        }

        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return smokeDuration;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, net.minecraft.util.EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        IDrugProperties props = playerIn.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (props instanceof DrugProperties && ((DrugProperties) props).isBreathingSmoke()) {
            return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.FAIL, itemstack);
        }

        playerIn.setActiveHand(handIn);
        return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, itemstack);
    }
}