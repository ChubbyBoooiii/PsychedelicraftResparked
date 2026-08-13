package com.chubbyboi.psychedelicraftresparked.config;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidSlurry;
import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import com.chubbyboi.psychedelicraftresparked.recipes.VatRecipes;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

@Mod.EventBusSubscriber
public class PSConfig {

    public static final String CATEGORY_CLIENT = "client";
    public static final String CATEGORY_SERVER = "server";
    public static final String CATEGORY_WORLDGEN = CATEGORY_SERVER + ".worldgen";

    private static Configuration config;

    public static Configuration getConfig() {
        return config;
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Tags.MOD_ID)) {
            loadConfig();
        }
    }

    public static boolean shader2DEnabled;
    public static boolean shader3DEnabled;
    public static boolean hallucinationEntitiesEnabled;
    public static boolean drugEffectsEnabled;
    public static boolean enableHarmonium;

    public static int dryingTableTickDuration;
    public static int slurryHardeningTime;
    public static int vatMixingTime;

    public static int randomTicksUntilRiftSpawn;

    public static float digitalEffectPixelRescaleX;
    public static float digitalEffectPixelRescaleY;

    public static boolean enableRiftJars;
    public static boolean riftJarOverfillingEnabled;

    public static boolean generateJuniper;
    public static boolean generateCannabis;
    public static boolean generateHop;
    public static boolean generateTobacco;
    public static boolean generateCoffea;
    public static boolean generateCoca;
    public static boolean generatePeyote;

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

        digitalEffectPixelRescaleX = (float) config.get(CATEGORY_CLIENT, "digitalEffectPixelRescaleX", 0.05,
            "Maximum horizontal pixelation of Zero's digital shader at full strength (1.0 = no pixelation, smaller = blockier). Default: 0.05").getDouble();
        digitalEffectPixelRescaleY = (float) config.get(CATEGORY_CLIENT, "digitalEffectPixelRescaleY", 0.05,
            "Maximum vertical pixelation of Zero's digital shader at full strength (1.0 = no pixelation, smaller = blockier). Default: 0.05").getDouble();

        enableRiftJars = config.get(CATEGORY_SERVER, "enableRiftJars", true,
            "Enables and disables the Rift Jar block/item/recipe - the only way to acquire Zero/Power essence from a Reality Rift. Default: true").getBoolean();

        riftJarOverfillingEnabled = config.get(CATEGORY_SERVER, "riftJarOverfillingEnabled", true,
            "Whether a Rift Jar can overfill past full when left sucking. If enabled, an overfilled jar breaks and explodes.\n"
                + "If disabled, the jar simply stops sucking once full - no overfilling, no breaking, no explosion. Default: true").getBoolean();

        config.addCustomCategoryComment(CATEGORY_WORLDGEN,
            "Toggles for each plant/tree that can naturally generate in the world. Chances are per-chunk,\n"
                + "rolled once against each listed biome type (a chunk only ever gets one roll, against\n"
                + "whichever of a plant's biome entries its biome matches first).");

        generateJuniper = config.get(CATEGORY_WORLDGEN, "generateJuniper", true,
            "Juniper trees. Found in: Cold+Hills biomes (10% chance), Cold+Forest biomes (5%), Snowy+Wasteland biomes (5%). Default: true").getBoolean();
        generateCannabis = config.get(CATEGORY_WORLDGEN, "generateCannabis", true,
            "Wild Cannabis plants. Found in: Plains biomes (4% chance), Forest biomes (4%). Default: true").getBoolean();
        generateHop = config.get(CATEGORY_WORLDGEN, "generateHop", true,
            "Wild Hop plants. Found in: Plains biomes (6% chance), Forest biomes (6%). Default: true").getBoolean();
        generateTobacco = config.get(CATEGORY_WORLDGEN, "generateTobacco", true,
            "Wild Tobacco plants. Found in: Plains biomes (4% chance), Forest biomes (4%). Default: true").getBoolean();
        generateCoffea = config.get(CATEGORY_WORLDGEN, "generateCoffea", true,
            "Wild Coffea plants. Found in: Plains biomes (5% chance), Forest biomes (5%). Default: true").getBoolean();
        generateCoca = config.get(CATEGORY_WORLDGEN, "generateCoca", true,
            "Wild Coca plants. Found in: Plains biomes (2% chance), Forest biomes (2%). Default: true").getBoolean();
        generatePeyote = config.get(CATEGORY_WORLDGEN, "generatePeyote", true,
            "Wild Peyote. Found in: Sandy+Hot biomes (4% chance), Mountain+Hot biomes (4%). Default: true").getBoolean();

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

        int defaultRandomTicksUntilRiftSpawn = 20 * 60 * 180;
        randomTicksUntilRiftSpawn = config.get(CATEGORY_SERVER, "randomTicksUntilRiftSpawn", defaultRandomTicksUntilRiftSpawn,
            "Average number of ticks between a Reality Rift randomly spawning near each online player (1-in-N chance per tick).\n"
                + "Negative disables new spawns (existing rifts are left alone). 0 disables spawning AND force-despawns any existing rifts.\n"
                + "Default: " + defaultRandomTicksUntilRiftSpawn + " (~3 hours)").getInt();

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