package com.chubbyboi.psychedelicraftresparked.capabilities;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.network.NetworkHandler;
import com.chubbyboi.psychedelicraftresparked.network.PacketResetHallucinations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityEventHandler {

    private static final ResourceLocation DRUG_PROPERTIES_CAP = new ResourceLocation(Tags.MOD_ID, "drug_properties");

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            // Attach our capability provider to the player
            event.addCapability(DRUG_PROPERTIES_CAP, new DrugPropertiesProvider());
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        // Don't copy on death - only copy when returning from End or dimension travel
        if (event.isWasDeath()) {
            if (event.getEntityPlayer() instanceof EntityPlayerMP) {
                NetworkHandler.INSTANCE.sendTo(new PacketResetHallucinations(), (EntityPlayerMP) event.getEntityPlayer());
            }
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        EntityPlayer oldPlayer = event.getOriginal();

        // Get capabilities from both old and new player
        IDrugProperties newProps = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        IDrugProperties oldProps = oldPlayer.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);

        // Copy data from old to new
        if (newProps != null && oldProps != null) {
            newProps.copyFrom(oldProps);
            PsychedelicraftResparked.LOGGER.debug("Copied drug properties after dimension change");
        }
    }
}