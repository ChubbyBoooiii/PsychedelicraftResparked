package com.chubbyboi.psychedelicraftresparked.drug;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.network.NetworkHandler;
import com.chubbyboi.psychedelicraftresparked.network.PacketSyncDrugProperties;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

public class DrugEffectHandler {

    private static final UUID DRUG_SPEED_MODIFIER_UUID = UUID.fromString("7f3a5c2e-9d1b-4a6f-8e3c-1a2b3c4d5e6f");
    private static final String DRUG_SPEED_MODIFIER_NAME = "Drug Speed Modifier";
    private static final int SLEEP_BLOCK_DELAY_TICKS = 60;

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) return;

        DrugProperties drugProps = (DrugProperties) props;

        // Get aggregated speed modifier from ALL drugs
        float speedMod = drugProps.getSpeedModifier();

        applySpeedModifier(player, speedMod);

        if (player.isPlayerSleeping() && drugProps.isSleepBlocked()) {
            if (drugProps.incrementSleepBlockedTicks() >= SLEEP_BLOCK_DELAY_TICKS) {
                player.wakeUpPlayer(false, true, false);
                player.sendStatusMessage(new TextComponentTranslation("psychedelicraftresparked.sleep.blocked"), true);
                drugProps.resetSleepBlockedTicks();
            }
        } else {
            drugProps.resetSleepBlockedTicks();
        }
    }

    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (event.wakeImmediately() || !event.shouldSetSpawn()) {
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) return;

        DrugProperties drugProps = (DrugProperties) props;
        drugProps.wakeUp();

        if (player instanceof EntityPlayerMP) {
            NetworkHandler.INSTANCE.sendTo(new PacketSyncDrugProperties(drugProps), (EntityPlayerMP) player);
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        IDrugProperties props = event.getEntityPlayer().getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) return;

        DrugProperties drugProps = (DrugProperties) props;

        // Get aggregated dig speed modifier from ALL drugs
        float digMod = drugProps.getDigSpeedModifier();

        event.setNewSpeed(event.getOriginalSpeed() * digMod);
    }

    private void applySpeedModifier(EntityLivingBase entity, float modifier) {
        IAttributeInstance movementSpeed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

        // Remove existing modifier
        AttributeModifier existing = movementSpeed.getModifier(DRUG_SPEED_MODIFIER_UUID);
        if (existing != null) {
            movementSpeed.removeModifier(existing);
        }

        // Apply new modifier if not 1.0
        if (Math.abs(modifier - 1.0f) > 0.001f) {
            AttributeModifier newModifier = new AttributeModifier(
                DRUG_SPEED_MODIFIER_UUID,
                DRUG_SPEED_MODIFIER_NAME,
                modifier - 1.0, // Attribute modifier is additive
                1 // Operation 1 = multiply base by (1 + amount)
            );
            movementSpeed.applyModifier(newModifier);
        }
    }
}
