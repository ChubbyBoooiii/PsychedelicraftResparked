package com.chubbyboi.psychedelicraftresparked.proxy;

import com.chubbyboi.psychedelicraftresparked.capabilities.CapabilityEventHandler;
import com.chubbyboi.psychedelicraftresparked.capabilities.CapabilityRegistry;
import com.chubbyboi.psychedelicraftresparked.drug.ChatDistortionHandler;
import com.chubbyboi.psychedelicraftresparked.drug.DrugEffectHandler;
import com.chubbyboi.psychedelicraftresparked.drug.DrugPropertyManager;
import com.chubbyboi.psychedelicraftresparked.entities.RiftSpawner;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {

    public void registerItemRenderer(Item item, int meta, String id) { }

    public void registerItemRenderer(Item item, int meta, String modelName, String id) { }

    public void registerRenderers() { }

    // Register capabilities and event handlers. Called during preInit from the main mod class.
    public void registerCapabilities() {
        // Register the drug properties capability with Forge
        CapabilityRegistry.registerCapabilities();

        // Register event handler for attaching capabilities to players (used for the drug strengths)
        MinecraftForge.EVENT_BUS.register(new CapabilityEventHandler());

        // Register drug property manager for decay and pending effects
        MinecraftForge.EVENT_BUS.register(new DrugPropertyManager());

        // Register server-side drug effect handler
        MinecraftForge.EVENT_BUS.register(new DrugEffectHandler());

        // Register the passive Reality Rift spawn roll
        MinecraftForge.EVENT_BUS.register(new RiftSpawner());

        // Register outgoing chat message distortion (Alcohol/Zero/Cannabis)
        MinecraftForge.EVENT_BUS.register(new ChatDistortionHandler());
    }
}