package com.chubbyboi.psychedelicraftresparked.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {

    // Missing the resparked as its gotta be less than 20 characters
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("psychedelicraft");

    private static int packetId = 0;

    public static void registerPackets() {
        // Drug property synchronisation (server -> client)
        INSTANCE.registerMessage(
            PacketSyncDrugProperties.Handler.class,
            PacketSyncDrugProperties.class,
            packetId++,
            Side.CLIENT
        );

        // Smoke particle spawning (server -> client)
        INSTANCE.registerMessage(
            PacketSpawnSmokeParticles.Handler.class,
            PacketSpawnSmokeParticles.class,
            packetId++,
            Side.CLIENT
        );

        // Hallucination-state reset signal on death (server -> client)
        INSTANCE.registerMessage(
            PacketResetHallucinations.Handler.class,
            PacketResetHallucinations.class,
            packetId++,
            Side.CLIENT
        );

        // Pipe of Smoke Monsters monster spawning (server -> client)
        INSTANCE.registerMessage(
            PacketSpawnSmokeMonster.Handler.class,
            PacketSpawnSmokeMonster.class,
            packetId++,
            Side.CLIENT
        );

        // Tinted custom-fluid splash particles (server -> client)
        INSTANCE.registerMessage(
            PacketSpawnFluidSplash.Handler.class,
            PacketSpawnFluidSplash.class,
            packetId++,
            Side.CLIENT
        );
    }
}