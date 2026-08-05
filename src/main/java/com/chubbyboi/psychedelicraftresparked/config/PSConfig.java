package com.chubbyboi.psychedelicraftresparked.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class PSConfig {

    public static final String CATEGORY_CLIENT = "client";
    public static final String CATEGORY_SERVER = "server";

    private static Configuration config;

    public static boolean shader2DEnabled;
    public static boolean shader3DEnabled;
    public static boolean hallucinationEntitiesEnabled;
    public static boolean drugEffectsEnabled;

    public static void loadConfig(File configFile) {
        config = new Configuration(configFile);
        loadConfig();
    }

    public static void loadConfig() {
        config.addCustomCategoryComment(CATEGORY_CLIENT,
            "Settings that only affect this client's own rendering - safe to differ between players, no effect on world state.");
        config.addCustomCategoryComment(CATEGORY_SERVER,
            "Settings affecting world/gameplay behaviour - ideally kept the same between server and client.");

        shader2DEnabled = config.get(CATEGORY_CLIENT, "shader2DEnabled", true,
            "Enables and disables all 2D (screen) drug shader effects, e.g. desaturation, bloom, double vision, motion blur.").getBoolean();
        shader3DEnabled = config.get(CATEGORY_CLIENT, "shader3DEnabled", true,
            "Enables and disables all 3D (world geometry) drug shader effects, e.g. waves, fractals, colour contrast.").getBoolean();
        hallucinationEntitiesEnabled = config.get(CATEGORY_CLIENT, "hallucinationEntitiesEnabled", true,
            "Enables and disables the fake hallucinated mobs (and the Rasta-Head easter egg) some drugs can spawn.").getBoolean();

        drugEffectsEnabled = config.get(CATEGORY_SERVER, "drugEffectsEnabled", true,
            "Enables and disables all drug effects server-wide, making every drug item purely cosmetic. Also wipes and blocks any existing/incoming drug levels, including via /druglevels set.").getBoolean();

        if (config.hasChanged()) {
            config.save();
        }
    }
}