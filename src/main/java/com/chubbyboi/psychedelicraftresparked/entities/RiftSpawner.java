package com.chubbyboi.psychedelicraftresparked.entities;

import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RiftSpawner {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
            return;
        }

        int chance = PSConfig.randomTicksUntilRiftSpawn;
        if (chance <= 0) {
            return;
        }

        EntityPlayer player = event.player;
        if (player.getRNG().nextInt(chance) == 0) {
            spawnRiftAtPlayer(player);
        }
    }

    public static void spawnRiftAtPlayer(EntityPlayer player) {
        double x = player.posX + (player.getRNG().nextDouble() - 0.5) * 100.0;
        double y = player.posY + (player.getRNG().nextDouble() - 0.5) * 100.0;
        double z = player.posZ + (player.getRNG().nextDouble() - 0.5) * 100.0;

        EntityRealityRift rift = new EntityRealityRift(player.world);
        rift.setPosition(x, y, z);
        player.world.spawnEntity(rift);
    }
}