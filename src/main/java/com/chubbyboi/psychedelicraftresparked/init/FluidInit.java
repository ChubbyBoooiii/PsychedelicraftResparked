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
    public static final FluidSlurry SLURRY = new FluidSlurry("slurry",
        new ResourceLocation("psychedelicraftresparked:blocks/slurry_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/slurry_flow")
    );

    public static final Fluid MILK = new Fluid("milk",
        new ResourceLocation("psychedelicraftresparked:blocks/clear_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/clear_flow")
    );



    // Fluid Drugs
    public static final FluidDrug COFFEE = new FluidDrug("coffee",
        new ResourceLocation("psychedelicraftresparked:blocks/clear_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/clear_flow")
    );

    public static final FluidDrug CANNABIS_TEA = new FluidDrug("cannabis_tea",
        new ResourceLocation("psychedelicraftresparked:blocks/tea_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/tea_flow")
    );

    public static final FluidDrug COCA_TEA = new FluidDrug("coca_tea",
        new ResourceLocation("psychedelicraftresparked:blocks/tea_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/tea_flow")
    );

    public static final FluidDrug PEYOTE_JUICE = new FluidDrug("peyote_juice",
        new ResourceLocation("psychedelicraftresparked:blocks/tea_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/tea_flow")
    );

    public static final FluidDrug COCAINE_FLUID = new FluidDrug("cocaine_fluid",
            new ResourceLocation("psychedelicraftresparked:blocks/clear_still"),
            new ResourceLocation("psychedelicraftresparked:blocks/clear_flow")
    );

    public static final FluidDrug CAFFEINE_FLUID = new FluidDrug("caffeine_fluid",
        new ResourceLocation("psychedelicraftresparked:blocks/clear_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/clear_flow")
    );



    // Alcoholic
    public static final FluidAlcohol WHEAT = new FluidAlcohol(
        "wheat",
        new ResourceLocation("psychedelicraftresparked:blocks/beer_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/beer_flow"),
        0.25, 1.7, 0.1,
        new FluidAlcohol.TickInfo(48000, 48000, 36000, 36000)
    );

    public static final FluidAlcohol MILK_ALCOHOL = new FluidAlcohol(
        "milk_alcohol",
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
        WHEAT.setColor(0xaafeaa08);
        WHEAT.addIcon(0, 3, 2, -1, new ResourceLocation("psychedelicraftresparked:blocks/clear_still"), new ResourceLocation("psychedelicraftresparked:blocks/clear_flow"));
        WHEAT.addIcon(4, 13, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_flow"));
        WHEAT.addIcon(14, -1, 0, -1, new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_still"), new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_flow"));

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
        FluidRegistry.registerFluid(WHEAT);
        FluidRegistry.registerFluid(MILK_ALCOHOL);
    }
}