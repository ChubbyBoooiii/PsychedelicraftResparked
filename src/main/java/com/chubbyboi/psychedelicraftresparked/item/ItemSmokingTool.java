package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.drug.DrugInfluence;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ItemSmokingTool extends PsychItem {

    public static class Consumable {
        public final ItemStack consumedItem;
        public final DrugInfluence[] drugInfluences;

        public final float[] smokeColor;

        public Consumable(ItemStack consumedItem, DrugInfluence[] drugInfluences) {
            this(consumedItem, drugInfluences, new float[]{1.0f, 1.0f, 1.0f});
        }

        public Consumable(ItemStack consumedItem, DrugInfluence[] drugInfluences, float[] smokeColor) {
            this.consumedItem = consumedItem;
            this.drugInfluences = drugInfluences;
            this.smokeColor = smokeColor;
        }
    }

    private final List<Consumable> consumables = new ArrayList<>();
    private final int useDuration;
    private EnumAction useAction = EnumAction.BOW;
    private boolean bubblingSound = false;

    public ItemSmokingTool(String name, int maxDamage, int useDuration) {
        super(name);
        setMaxDamage(maxDamage);
        setMaxStackSize(1);
        this.useDuration = useDuration;

        this.addPropertyOverride(new ResourceLocation("smoking"), (stack, worldIn, entityIn) ->
            entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F
        );
    }

    public ItemSmokingTool addConsumable(Consumable consumable) {
        consumables.add(consumable);
        return this;
    }

    public ItemSmokingTool setUseAction(EnumAction useAction) {
        this.useAction = useAction;
        return this;
    }

    public ItemSmokingTool setBubblingSound(boolean bubblingSound) {
        this.bubblingSound = bubblingSound;
        return this;
    }

    protected Consumable getUsedConsumable(EntityPlayer player) {
        for (Consumable consumable : consumables) {
            if (findMatchingStack(player, consumable.consumedItem) != null) {
                return consumable;
            }
        }
        return null;
    }

    private static ItemStack findMatchingStack(EntityPlayer player, ItemStack target) {
        InventoryPlayer inventory = player.inventory;
        for (ItemStack stack : inventory.mainInventory) {
            if (!stack.isEmpty() && stack.isItemEqual(target)) return stack;
        }
        for (ItemStack stack : inventory.offHandInventory) {
            if (!stack.isEmpty() && stack.isItemEqual(target)) return stack;
        }
        return null;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        if (bubblingSound && !player.world.isRemote && player instanceof EntityPlayerMP && player.world.rand.nextInt(3) == 0) {
            ((EntityPlayerMP) player).connection.sendPacket(new SPacketSoundEffect(SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 1.0F, 1.0F));
        }
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return useAction;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return useDuration;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (!(entityLiving instanceof EntityPlayer)) {
            return stack;
        }
        EntityPlayer player = (EntityPlayer) entityLiving;

        Consumable usedConsumable = getUsedConsumable(player);
        if (usedConsumable == null) {
            return stack;
        }

        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);

        if (!worldIn.isRemote) {
            ItemStack ingredient = findMatchingStack(player, usedConsumable.consumedItem);
            if (ingredient == null) {
                return stack;
            }

            ingredient.shrink(1);

            if (props instanceof DrugProperties) {
                for (DrugInfluence influence : usedConsumable.drugInfluences) {
                    ((DrugProperties) props).addInfluence(influence.clone());
                }
            }

            if (!player.capabilities.isCreativeMode) {
                stack.damageItem(1, player);
            }
        }

        if (props instanceof DrugProperties) {
            ((DrugProperties) props).startBreathingSmoke(getBreathingSmokeDuration(player.world), usedConsumable.smokeColor, getSmokeSpeedMultiplier());
        }

        return stack;
    }

    protected int getBreathingSmokeDuration(World world) {
        return 10 + world.rand.nextInt(10);
    }

    protected float getSmokeSpeedMultiplier() {
        return 1.0f;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        IDrugProperties props = playerIn.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        boolean breathingSmoke = props instanceof DrugProperties && ((DrugProperties) props).isBreathingSmoke();

        if (!breathingSmoke && getUsedConsumable(playerIn) != null) {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        }

        return new ActionResult<>(EnumActionResult.FAIL, itemstack);
    }
}
