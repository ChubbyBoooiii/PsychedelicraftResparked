package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.drug.DrugInfluence;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class ItemIngestable extends PsychFoodItem {

    // These gotta be set in registration
    protected String drugType;
    protected int onsetDelay;
    protected double influenceSpeed;
    protected double influenceSpeedPlus;
    protected double maxInfluence;

    // Behaviours can be set in reg if we're not wanting the defaults
    protected int consumeDuration = 32; // In ticks
    protected EnumAction useAction = EnumAction.EAT;
    protected SoundEvent finishSound = SoundEvents.ENTITY_PLAYER_BURP;

    public ItemIngestable(String name, int amount, float saturationMultiplier) {
        super(name, amount, saturationMultiplier);
        this.setMaxStackSize(16);
    }

    public ItemIngestable setDrugInfluence(String drugType, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence) {
        this.drugType = drugType;
        this.onsetDelay = delay;
        this.influenceSpeed = influenceSpeed;
        this.influenceSpeedPlus = influenceSpeedPlus;
        this.maxInfluence = maxInfluence;
        return this;
    }

    public ItemIngestable setConsumeDuration(int duration) {
        this.consumeDuration = duration;
        return this;
    }

    public ItemIngestable setUseAction(EnumAction useAction) {
        this.useAction = useAction;
        return this;
    }

    public ItemIngestable setFinishSound(SoundEvent finishSound) {
        this.finishSound = finishSound;
        return this;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;

            if (!worldIn.isRemote) {
                // Validate drug parameters are set
                if (drugType == null) {
                    throw new IllegalStateException("ItemIngestable '" + this.getRegistryName() + "' is missing drug parameters!");
                }

                // Get drug properties
                IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);

                if (props instanceof DrugProperties) {
                    DrugProperties drugProps = (DrugProperties) props;

                    DrugInfluence influence = new DrugInfluence(
                        drugType,
                        onsetDelay,
                        influenceSpeed,
                        influenceSpeedPlus,
                        maxInfluence
                    );
                    // Add to player
                    drugProps.addInfluence(influence);
                }
            }

            player.getFoodStats().addStats(this, stack);

            if (finishSound != null) {
                worldIn.playSound(null, player.posX, player.posY, player.posZ, finishSound, SoundCategory.PLAYERS, 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
            }

            onFoodEaten(stack, worldIn, player);
            player.addStat(StatList.getObjectUseStats(this));

            if (player instanceof EntityPlayerMP) {
                CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) player, stack);
            }

            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }

        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return consumeDuration;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return useAction;
    }

    @Override
    public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, net.minecraft.util.EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, itemstack);
    }
}