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
import java.util.ArrayList;
import java.util.Arrays;

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

    public static boolean simpleShadersEnabled;
    public static boolean advancedShadersEnabled;
    public static boolean skyDrugEffects;
    public static boolean hallucinationEntitiesEnabled;
    public static boolean biomeHeatDistortion;
    public static boolean waterDistortion;
    public static boolean drugEffectsEnabled;
    public static boolean enableHarmonium;
    public static boolean distortOutgoingMessages;

    public static int dryingTableTickDuration;
    public static int slurryHardeningTime;
    public static int vatMixingTime;

    public static int randomTicksUntilRiftSpawn;

    public static float digitalEffectPixelRescaleX;
    public static float digitalEffectPixelRescaleY;

    public static float sunFlareIntensity;
    public static int[] sunFlareDisabledDimensions;

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

        simpleShadersEnabled = config.get(CATEGORY_CLIENT, "simpleShadersEnabled", true,
            "Enables and disables simple screen-space drug shader effects, e.g. desaturation, bloom, double vision, motion blur. Default: true").getBoolean();
        advancedShadersEnabled = config.get(CATEGORY_CLIENT, "advancedShadersEnabled", true,
            "Enables and disables advanced/complex drug shader effects, e.g. 3D world geometry (waves, fractals, colour contrast)\n"
                + "and Zero's digital/glitch effect. These are the most likely to conflict with OptiFine shaderpacks - disable this\n"
                + "first if you're seeing rendering glitches with a shaderpack active. Default: true").getBoolean();
        skyDrugEffects = config.get(CATEGORY_CLIENT, "skyDrugEffects", true,
            "Whether the advanced world effects (pulses, fractals, colour contrast) also reach the sky. Turn this off if a mod\n"
                + "with its own sky renderer looks wrong with them. Needs advancedShadersEnabled. Default: true").getBoolean();
        hallucinationEntitiesEnabled = config.get(CATEGORY_CLIENT, "hallucinationEntitiesEnabled", true,
            "Enables and disables the fake hallucinated mobs (and the Rasta-Head easter egg) some drugs can spawn. Default: true").getBoolean();
        biomeHeatDistortion = config.get(CATEGORY_CLIENT, "biomeHeatDistortion", true,
            "Enables and disables the heat shimmer on distant terrain in hot biomes (desert, mesa, Nether). Not a drug effect,\n"
                + "unaffected by simpleShadersEnabled/advancedShadersEnabled. Default: true").getBoolean();
        waterDistortion = config.get(CATEGORY_CLIENT, "waterDistortion", true,
            "Enables and disables the wobble distortion while your view is underwater. Not a drug effect,\n"
                + "unaffected by simpleShadersEnabled/advancedShadersEnabled. Default: true").getBoolean();
        sunFlareIntensity = (float) config.get(CATEGORY_CLIENT, "sunFlareIntensity", 0.25,
            "Intensity of the lens flare and glare when looking at the sun. Set to 0 to disable it entirely. Not a drug effect,\n"
                + "unaffected by simpleShadersEnabled/advancedShadersEnabled. Default: 0.25").getDouble();
        sunFlareDisabledDimensions = config.get(CATEGORY_CLIENT, "sunFlareDisabledDimensions", new int[0],
            "Dimension IDs where the sun flare never shows, e.g. modded dimensions without a sun. Not needed for the\n"
                + "Nether or End, which never show one. Default: none").getIntList();

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
            "Enables and disables all drug effects server-wide, making every drug item purely cosmetic.\nAlso wipes and blocks any existing/incoming drug levels, including via /psyche druglevels set. Default: true").getBoolean();

        enableHarmonium = config.get(CATEGORY_SERVER, "enableHarmonium", true,
            "Enables and disables the fictional drug Harmonium's crafting recipes (16 dye colours). Default: true").getBoolean();

        distortOutgoingMessages = config.get(CATEGORY_SERVER, "distortOutgoingMessages", true,
            "Whether a player's own chat messages get slurred/glitched (Alcohol/Zero) or padded with filler words (Cannabis)\n"
                + "based on their own drug levels, visible to everyone. Default: true").getBoolean();

        int defaultDryingTableTickDuration = 3600;
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

        config.setCategoryPropertyOrder(CATEGORY_CLIENT, new ArrayList<>(Arrays.asList(
            "simpleShadersEnabled", "advancedShadersEnabled", "skyDrugEffects",
            "digitalEffectPixelRescaleX", "digitalEffectPixelRescaleY",
            "biomeHeatDistortion", "waterDistortion",
            "sunFlareIntensity", "sunFlareDisabledDimensions",
            "hallucinationEntitiesEnabled")));
        config.setCategoryPropertyOrder(CATEGORY_SERVER, new ArrayList<>(Arrays.asList(
            "drugEffectsEnabled", "distortOutgoingMessages",
            "enableHarmonium",
            "dryingTableTickDuration", "vatMixingTime", "slurryHardeningTime",
            "enableRiftJars", "riftJarOverfillingEnabled", "randomTicksUntilRiftSpawn")));
        config.setCategoryPropertyOrder(CATEGORY_WORLDGEN, new ArrayList<>(Arrays.asList(
            "generateJuniper", "generateCannabis", "generateHop", "generateTobacco",
            "generateCoffea", "generateCoca", "generatePeyote")));
        config.save();
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