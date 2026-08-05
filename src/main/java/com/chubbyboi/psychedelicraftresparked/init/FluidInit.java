package com.chubbyboi.psychedelicraftresparked.init;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidDrug;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidSlurry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidInit {

    // ==================== INIT FLUIDS ====================
    // Base Fluids
    public static final FluidSlurry SLURRY = new FluidSlurry("psc_slurry",
        new ResourceLocation("psychedelicraftresparked:blocks/slurry_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/slurry_flow")
    );

    public static final Fluid MILK = new Fluid("psc_milk_raw",
        new ResourceLocation("psychedelicraftresparked:blocks/clear_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/clear_flow")
    );



    // Fluid Drugs
    public static final FluidDrug COFFEE = new FluidDrug("psc_coffee",
        new ResourceLocation("psychedelicraftresparked:blocks/clear_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/clear_flow")
    );

    public static final FluidDrug CANNABIS_TEA = new FluidDrug("psc_cannabis_tea",
        new ResourceLocation("psychedelicraftresparked:blocks/tea_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/tea_flow")
    );

    public static final FluidDrug COCA_TEA = new FluidDrug("psc_coca_tea",
        new ResourceLocation("psychedelicraftresparked:blocks/tea_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/tea_flow")
    );

    public static final FluidDrug PEYOTE_JUICE = new FluidDrug("psc_peyote_juice",
        new ResourceLocation("psychedelicraftresparked:blocks/tea_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/tea_flow")
    );

    public static final FluidDrug COCAINE_FLUID = new FluidDrug("psc_cocaine_fluid",
            new ResourceLocation("psychedelicraftresparked:blocks/clear_still"),
            new ResourceLocation("psychedelicraftresparked:blocks/clear_flow")
    );

    public static final FluidDrug CAFFEINE_FLUID = new FluidDrug("psc_caffeine_fluid",
        new ResourceLocation("psychedelicraftresparked:blocks/clear_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/clear_flow")
    );



    // Alcoholic
    public static final FluidAlcohol WHEAT_HOP = new FluidAlcohol(
        "psc_wheat_hop",
        new ResourceLocation("psychedelicraftresparked:blocks/beer_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/beer_flow"),
        0.25, 1.7, 0.1,
        new FluidAlcohol.TickInfo(36000, 72000, 120000, 36000)
    );

    public static final FluidAlcohol WHEAT = new FluidAlcohol(
        "psc_wheat",
        new ResourceLocation("psychedelicraftresparked:blocks/beer_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/beer_flow"),
        0.25, 1.7, 0.1,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );

    public static final FluidAlcohol CORN = new FluidAlcohol(
        "psc_corn",
        new ResourceLocation("psychedelicraftresparked:blocks/beer_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/beer_flow"),
        0.25, 1.7, 0.1,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );

    public static final FluidAlcohol POTATO = new FluidAlcohol(
        "psc_potato",
        new ResourceLocation("psychedelicraftresparked:blocks/beer_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/beer_flow"),
        0.45, 1.9, 0.15,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );

    public static final FluidAlcohol GRAPES = new FluidAlcohol(
        "psc_grapes",
        new ResourceLocation("psychedelicraftresparked:blocks/wine_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/wine_flow"),
        0.55, 1.7, 0.2,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );

    public static final FluidAlcohol RICE = new FluidAlcohol(
        "psc_rice",
        new ResourceLocation("psychedelicraftresparked:blocks/rice_wine_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/rice_wine_flow"),
        0.25, 1.7, 0.1,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );

    public static final FluidAlcohol HONEY = new FluidAlcohol(
        "psc_honey",
        new ResourceLocation("psychedelicraftresparked:blocks/mead_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/mead_flow"),
        0.35, 1.7, 0.1,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );

    public static final FluidAlcohol JUNIPER = new FluidAlcohol(
        "psc_juniper",
        new ResourceLocation("psychedelicraftresparked:blocks/slurry_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/slurry_flow"),
        0.4, 1.7, 0.1,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );

    public static final FluidAlcohol SUGAR_CANE = new FluidAlcohol(
        "psc_sugar_cane",
        new ResourceLocation("psychedelicraftresparked:blocks/clear_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/clear_flow"),
        0.35, 1.7, 0.1,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );

    public static final FluidAlcohol APPLE = new FluidAlcohol(
        "psc_apple",
        new ResourceLocation("psychedelicraftresparked:blocks/cider_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/cider_flow"),
        0.35, 1.7, 0.1,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );

    public static final FluidAlcohol PINEAPPLE = new FluidAlcohol(
        "psc_pineapple",
        new ResourceLocation("psychedelicraftresparked:blocks/cider_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/cider_flow"),
        0.35, 1.7, 0.1,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );

    public static final FluidAlcohol BANANA = new FluidAlcohol(
        "psc_banana",
        new ResourceLocation("psychedelicraftresparked:blocks/mead_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/mead_flow"),
        0.35, 1.7, 0.1,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );

    public static final FluidAlcohol MILK_ALCOHOL = new FluidAlcohol(
        "psc_milk_alcohol",
        new ResourceLocation("psychedelicraftresparked:blocks/rice_wine_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/rice_wine_flow"),
        0.35, 1.7, 0.1,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );



    // ==================== FLUID PROPERTIES ====================
    static {
        // Base Fluids
        SLURRY.setColor(0xcc704e21);

        MILK.setColor(0xfffdf7ec);



        // Fluid Drugs
        COFFEE.setDrinkable(true);
        COFFEE.setColor(0xffa77d55);
        COFFEE.addDrugInfluencePerBucket("caffeine", 20, 0.002, 0.001, 0.25);

        CANNABIS_TEA.setDrinkable(true);
        CANNABIS_TEA.setColor(0xff6d6f3c);
        CANNABIS_TEA.addDrugInfluencePerBucket("cannabis", 60, 0.005, 0.002, 0.25);

        COCA_TEA.setDrinkable(true);
        COCA_TEA.setColor(0xff787a36);
        COCA_TEA.addDrugInfluencePerBucket("cocaine", 60, 0.005, 0.002, 0.2);

        PEYOTE_JUICE.setDrinkable(true);
        PEYOTE_JUICE.setColor(0xff9bab62);
        PEYOTE_JUICE.addDrugInfluencePerBucket("peyote", 15, 0.005, 0.003, 2.0);

        COCAINE_FLUID.setInjectable(true);
        COCAINE_FLUID.setColor(0x44e8f4f8);
        COCAINE_FLUID.addDrugInfluencePerBucket("cocaine", 0, 0.005, 0.01, 50.0);

        CAFFEINE_FLUID.setInjectable(true);
        CAFFEINE_FLUID.setColor(0x66eee2d3);
        CAFFEINE_FLUID.addDrugInfluencePerBucket("caffeine", 0, 0.005, 0.01, 85.0);



        // Alcoholic
        WHEAT_HOP.setColor(0xaafeaa08);
        WHEAT_HOP.addIcon(0, 3, 2, -1, new ResourceLocation("psychedelicraftresparked:blocks/clear_still"), new ResourceLocation("psychedelicraftresparked:blocks/clear_flow"));
        WHEAT_HOP.addIcon(4, 13, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_flow"));
        WHEAT_HOP.addIcon(14, -1, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_flow"));

        WHEAT.setColor(0xaafeaa08);
        WHEAT.addIcon(0, 3, 2, -1, new ResourceLocation("psychedelicraftresparked:blocks/clear_still"), new ResourceLocation("psychedelicraftresparked:blocks/clear_flow"));
        WHEAT.addIcon(4, 13, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_flow"));
        WHEAT.addIcon(14, -1, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_flow"));

        CORN.setColor(0xaafeaa08);
        CORN.addIcon(0, 3, 2, -1, new ResourceLocation("psychedelicraftresparked:blocks/clear_still"), new ResourceLocation("psychedelicraftresparked:blocks/clear_flow"));
        CORN.addIcon(4, 13, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_flow"));
        CORN.addIcon(14, -1, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_flow"));

        POTATO.setColor(0xaafeaa08);
        POTATO.addIcon(0, 3, 2, -1, new ResourceLocation("psychedelicraftresparked:blocks/clear_still"), new ResourceLocation("psychedelicraftresparked:blocks/clear_flow"));
        POTATO.addIcon(4, 13, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_flow"));
        POTATO.addIcon(14, -1, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_flow"));

        GRAPES.setColor(0xee3f0822);
        GRAPES.setDistilledColor(0x993f0822);
        GRAPES.setMatureColor(0xee3f0822);

        RICE.setColor(0xeecac4b2);
        RICE.setMatureColor(0x88D6BC90);
        RICE.addIcon(0, -1, 2, -1, new ResourceLocation("psychedelicraftresparked:blocks/clear_still"), new ResourceLocation("psychedelicraftresparked:blocks/clear_flow"));

        HONEY.setColor(0xbbe9ae3b);
        HONEY.setDistilledColor(0x99e9ae3b);
        HONEY.setMatureColor(0xaaD1984D);

        JUNIPER.setColor(0xcc704e21);
        JUNIPER.addIcon(0, 3, 2, -1, new ResourceLocation("psychedelicraftresparked:blocks/clear_still"), new ResourceLocation("psychedelicraftresparked:blocks/clear_flow"));
        JUNIPER.addIcon(4, 13, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_flow"));
        JUNIPER.addIcon(14, -1, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_flow"));

        SUGAR_CANE.setColor(0xcc704e21);
        SUGAR_CANE.addIcon(0, 3, 2, -1, new ResourceLocation("psychedelicraftresparked:blocks/clear_still"), new ResourceLocation("psychedelicraftresparked:blocks/clear_flow"));
        SUGAR_CANE.addIcon(4, 13, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_flow"));
        SUGAR_CANE.addIcon(14, -1, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_flow"));

        APPLE.setColor(0x99edc13b);
        APPLE.setDistilledColor(0x66edc13b);
        APPLE.setMatureColor(0x88edc13b);

        PINEAPPLE.setColor(0x99edc13b);
        PINEAPPLE.setDistilledColor(0x66edc13b);
        PINEAPPLE.setMatureColor(0x88edc13b);

        BANANA.setColor(0xbbe9ae3b);
        BANANA.setDistilledColor(0x99e9ae3b);
        BANANA.setMatureColor(0xaaD1984D);

        MILK_ALCOHOL.setColor(0x77cac4b2);
        MILK_ALCOHOL.setMatureColor(0x88D6BC90);
    }



    // Register
    public static void registerFluids() {
        FluidRegistry.registerFluid(MILK);
        FluidRegistry.registerFluid(SLURRY);
        FluidRegistry.registerFluid(COFFEE);
        FluidRegistry.registerFluid(CANNABIS_TEA);
        FluidRegistry.registerFluid(COCA_TEA);
        FluidRegistry.registerFluid(PEYOTE_JUICE);
        FluidRegistry.registerFluid(COCAINE_FLUID);
        FluidRegistry.registerFluid(CAFFEINE_FLUID);
        FluidRegistry.registerFluid(WHEAT_HOP);
        FluidRegistry.registerFluid(WHEAT);
        FluidRegistry.registerFluid(CORN);
        FluidRegistry.registerFluid(POTATO);
        FluidRegistry.registerFluid(GRAPES);
        FluidRegistry.registerFluid(RICE);
        FluidRegistry.registerFluid(HONEY);
        FluidRegistry.registerFluid(JUNIPER);
        FluidRegistry.registerFluid(SUGAR_CANE);
        FluidRegistry.registerFluid(APPLE);
        FluidRegistry.registerFluid(PINEAPPLE);
        FluidRegistry.registerFluid(BANANA);
        FluidRegistry.registerFluid(MILK_ALCOHOL);
    }
}