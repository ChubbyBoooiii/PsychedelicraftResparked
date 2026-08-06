package com.chubbyboi.psychedelicraftresparked.config;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidSlurry;
import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import com.chubbyboi.psychedelicraftresparked.recipes.VatRecipes;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
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
    public static boolean enableHarmonium;

    public static int dryingTableTickDuration;
    public static int slurryHardeningTime;
    public static int vatMixingTime;

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
            "Enables and disables all 2D (screen) drug shader effects, e.g. desaturation, bloom, double vision, motion blur. Default: true").getBoolean();
        shader3DEnabled = config.get(CATEGORY_CLIENT, "shader3DEnabled", true,
            "Enables and disables all 3D (world geometry) drug shader effects, e.g. waves, fractals, colour contrast. Default: true").getBoolean();
        hallucinationEntitiesEnabled = config.get(CATEGORY_CLIENT, "hallucinationEntitiesEnabled", true,
            "Enables and disables the fake hallucinated mobs (and the Rasta-Head easter egg) some drugs can spawn. Default: true").getBoolean();

        drugEffectsEnabled = config.get(CATEGORY_SERVER, "drugEffectsEnabled", true,
            "Enables and disables all drug effects server-wide, making every drug item purely cosmetic.\nAlso wipes and blocks any existing/incoming drug levels, including via /druglevels set. Default: true").getBoolean();

        enableHarmonium = config.get(CATEGORY_SERVER, "enableHarmonium", true,
            "Enables and disables the fictional drug Harmonium's crafting recipes (16 dye colours). Default: true").getBoolean();

        int defaultDryingTableTickDuration = 2000;
        dryingTableTickDuration = config.get(CATEGORY_SERVER, "dryingTableTickDuration", defaultDryingTableTickDuration,
            "Base number of ticks the Drying Table takes to dry a full stack of items (actual time also depends on light and biome temperature). Default: "
                + defaultDryingTableTickDuration).getInt();

        int defaultSlurryHardeningTime = FluidSlurry.HARDENING_TIME;
        slurryHardeningTime = config.get(CATEGORY_SERVER, "slurryHardeningTime", defaultSlurryHardeningTime,
            "Number of ticks it takes a full Vat of Slurry to harden into dirt. Default: " + defaultSlurryHardeningTime).getInt();
        FluidSlurry.HARDENING_TIME = slurryHardeningTime;

        int defaultVatMixingTime = TileEntityVat.MIXING_TIME;
        vatMixingTime = config.get(CATEGORY_SERVER, "vatMixingTime", defaultVatMixingTime,
            "Number of ticks the Vat spends mixing ingredients into the fluid before fermentation begins. Default: " + defaultVatMixingTime).getInt();
        TileEntityVat.MIXING_TIME = vatMixingTime;

        config.addCustomCategoryComment(CATEGORY_SERVER + ".fluids",
            "Tick timings for each alcohol fluid's processing stages:\nticksPerFermentation (wort -> wash),\n"
                + "ticksPerDistillation (each distillation pass),\n"
                + "ticksPerMaturation (each maturation step in a Barrel),\n"
                + "ticksUntilAcetification (time before turning to vinegar, -1 to disable).");

        for (FluidAlcohol fluid : FluidInit.ALL_ALCOHOLS) {
            readTickInfo(fluid);
        }

        config.addCustomCategoryComment(CATEGORY_SERVER + ".oredict",
            "OreDictionary tag names each Vat recipe ingredient will accept, purely oreDict-driven.\n"
                + "Editing a list here replaces the built-in default entirely.");

        for (VatRecipes.Recipe recipe : VatRecipes.getInstance().getRecipes()) {
            readOreDictNames(recipe);
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    private static void readTickInfo(FluidAlcohol fluid) {
        String rawName = fluid.getName();
        String shortName = rawName.startsWith("psc_") ? rawName.substring(4) : rawName;
        String category = CATEGORY_SERVER + ".fluids." + shortName;
        FluidAlcohol.TickInfo tickInfo = fluid.getTickInfo();

        int defaultFermentation = tickInfo.ticksPerFermentation;
        int defaultDistillation = tickInfo.ticksPerDistillation;
        int defaultMaturation = tickInfo.ticksPerMaturation;
        int defaultAcetification = tickInfo.ticksUntilAcetification;

        tickInfo.ticksPerFermentation = config.get(category, "ticksPerFermentation", defaultFermentation,
            "Default: " + defaultFermentation).getInt();
        tickInfo.ticksPerDistillation = config.get(category, "ticksPerDistillation", defaultDistillation,
            "Default: " + defaultDistillation).getInt();
        tickInfo.ticksPerMaturation = config.get(category, "ticksPerMaturation", defaultMaturation,
            "Default: " + defaultMaturation).getInt();
        tickInfo.ticksUntilAcetification = config.get(category, "ticksUntilAcetification", defaultAcetification,
            "Default: " + defaultAcetification).getInt();
    }

    private static void readOreDictNames(VatRecipes.Recipe recipe) {
        String rawName = recipe.getOutput().getName();
        String shortName = rawName.startsWith("psc_") ? rawName.substring(4) : rawName;
        String category = CATEGORY_SERVER + ".oredict." + shortName;

        for (VatRecipes.IngredientEntry entry : recipe.getIngredients()) {
            String[] defaultOreNames = entry.getDefaultOreNames();
            String[] configuredOreNames = config.get(category, entry.getKey(), defaultOreNames,
                "Default: " + String.join(", ", defaultOreNames)).getStringList();
            entry.setOreNames(configuredOreNames);
        }
    }
}