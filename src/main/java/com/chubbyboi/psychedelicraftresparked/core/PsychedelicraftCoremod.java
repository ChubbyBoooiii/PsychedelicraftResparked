package com.chubbyboi.psychedelicraftresparked.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Psychedelicraft Resparked Core")
public class PsychedelicraftCoremod implements IFMLLoadingPlugin {

    public PsychedelicraftCoremod() {
        // Initialize Mixin if not already done
        MixinBootstrap.init();

        System.out.println("[Psychedelicraft Resparked] About to register mixin config...");

        // Register our mixin config
        try {
            Mixins.addConfiguration("mixins.psychedelicraftresparked.json");
            System.out.println("[Psychedelicraft Resparked] Successfully registered mixin config!");
        } catch (Exception e) {
            System.out.println("[Psychedelicraft Resparked] ERROR registering mixin config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}