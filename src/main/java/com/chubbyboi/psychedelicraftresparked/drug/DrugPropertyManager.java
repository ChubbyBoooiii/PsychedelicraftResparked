package com.chubbyboi.psychedelicraftresparked.drug;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.capabilities.DrugPropertiesProvider;
import com.chubbyboi.psychedelicraftresparked.capabilities.IDrugProperties;
import com.chubbyboi.psychedelicraftresparked.network.NetworkHandler;
import com.chubbyboi.psychedelicraftresparked.network.PacketSyncDrugProperties;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DrugPropertyManager {

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return;
        }

        // Tick all online players
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
    }

    private void tickPlayer(EntityPlayer player) {
        IDrugProperties props = player.getCapability(DrugPropertiesProvider.DRUG_PROPERTIES_CAPABILITY, null);
        if (!(props instanceof DrugProperties)) return;

        DrugProperties drugProps = (DrugProperties) props;

        // Check if player has any active or desired drugs
        boolean hasAnyDrugs = false;
        for (IDrug drug : drugProps.getAllDrugs()) {
            if (drug.getActiveValue() > 0.001f || drug.getDesiredValue() > 0.001f) {
                hasAnyDrugs = true;
                break;
            }
        }

        // Check if player has any active influences
        boolean hasInfluences = drugProps.hasActiveInfluences();

        // Early exit if no drugs or influences
        if (!hasAnyDrugs && !hasInfluences && !drugProps.isBreathingSmoke()) {
            return;
        }

        // Update drug states (decay + influences happen inside DrugProperties)
        drugProps.update(player);

        // Sync to client
        syncToClient(player, drugProps);
    }

    private void syncToClient(EntityPlayer player, IDrugProperties props) {
        if (player instanceof EntityPlayerMP) {
            PacketSyncDrugProperties packet = new PacketSyncDrugProperties(props);
            NetworkHandler.INSTANCE.sendTo(packet, (EntityPlayerMP) player);
        }
    }
}