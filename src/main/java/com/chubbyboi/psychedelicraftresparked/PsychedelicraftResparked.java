package com.chubbyboi.psychedelicraftresparked;

import com.chubbyboi.psychedelicraftresparked.commands.CommandDrugLevels;
import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import com.chubbyboi.psychedelicraftresparked.network.NetworkHandler;
import com.chubbyboi.psychedelicraftresparked.proxy.CommonProxy;
import com.chubbyboi.psychedelicraftresparked.tabs.PsychedelicraftResparkedDrinksTab;
import com.chubbyboi.psychedelicraftresparked.tabs.PsychedelicraftResparkedTab;
import com.chubbyboi.psychedelicraftresparked.util.RegistryHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, guiFactory = "com.chubbyboi.psychedelicraftresparked.config.PSConfigGuiFactory")
public class PsychedelicraftResparked {

    @Mod.Instance
    public static PsychedelicraftResparked instance;

    @SidedProxy(clientSide = "com.chubbyboi.psychedelicraftresparked.proxy.ClientProxy", serverSide = "com.chubbyboi.psychedelicraftresparked.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static final CreativeTabs PSYCHTAB = new PsychedelicraftResparkedTab("psychedelicraftresparkedtab");
    public static final CreativeTabs DRINKS_TAB = new PsychedelicraftResparkedDrinksTab("psychedelicraftresparkeddrinkstab");

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Load mod config first, since registries below may need to consult it
        PSConfig.loadConfig(event.getSuggestedConfigurationFile());

        // Register mixin config
        Mixins.addConfiguration("mixins.psychedelicraftresparked.json");

        // Register capabilities first
        proxy.registerCapabilities();

        // Register network packets
        NetworkHandler.registerPackets();

        RegistryHandler.preInitRegistries();
        proxy.registerRenderers();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        RegistryHandler.initRegistries();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        RegistryHandler.postInitRegistries();
    }

    @Mod.EventHandler
    public void serverInit(FMLServerStartingEvent event) {
        RegistryHandler.serverRegistries(event);

        event.registerServerCommand(new CommandDrugLevels());
    }
}