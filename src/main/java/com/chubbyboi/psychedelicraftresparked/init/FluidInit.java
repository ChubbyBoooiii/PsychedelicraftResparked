package com.chubbyboi.psychedelicraftresparked.init;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidAlcohol;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidDrug;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidSlurry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidInit {

    public static final FluidSlurry SLURRY = new FluidSlurry("slurry",
        new ResourceLocation("psychedelicraftresparked:blocks/slurry_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/slurry_flow")
    );

    static {
        SLURRY.setColor(0xcc704e21);
    }

    public static final FluidDrug COFFEE = new FluidDrug("coffee",
        new ResourceLocation("psychedelicraftresparked:blocks/clear_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/clear_flow")
    );

    static {
        COFFEE.setDrinkable(true);
        COFFEE.setColor(0xffa77d55);
        COFFEE.addDrugInfluencePerBucket("caffeine", 20, 0.002, 0.001, 0.25);
    }

    public static final FluidDrug COCAINE_FLUID = new FluidDrug("cocaine_fluid",
        new ResourceLocation("psychedelicraftresparked:blocks/clear_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/clear_flow")
    );

    static {
        COCAINE_FLUID.setInjectable(true);
        COCAINE_FLUID.setColor(0x44e8f4f8);
        COCAINE_FLUID.addDrugInfluencePerBucket("cocaine", 0, 0.005, 0.01, 50.0);
    }

    // ==================== ALCOHOL ====================

    public static final FluidAlcohol WHEAT = new FluidAlcohol(
        "wheat",
        new ResourceLocation("psychedelicraftresparked:blocks/beer_still"),
        new ResourceLocation("psychedelicraftresparked:blocks/beer_flow"),
        0.25, 1.7, 0.1,
        FluidAlcohol.TickInfo.ofMinutes(40, 40, 30, 30)
    );

    static {
        WHEAT.setColor(0xaafeaa08);
        WHEAT.addIcon(0, 3, 2, -1,
            new ResourceLocation("psychedelicraftresparked:blocks/clear_still"),
            new ResourceLocation("psychedelicraftresparked:blocks/clear_flow"));
        WHEAT.addIcon(4, 13, 0, -1,
            new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_still"),
            new ResourceLocation("psychedelicraftresparked:blocks/rum_semi_mature_flow"));
        WHEAT.addIcon(14, -1, 0, -1,
            new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_still"),
            new ResourceLocation("psychedelicraftresparked:blocks/rum_mature_flow"));
    }

    public static void registerFluids() {
        FluidRegistry.registerFluid(COFFEE);
        FluidRegistry.registerFluid(COCAINE_FLUID);
        FluidRegistry.registerFluid(WHEAT);
        FluidRegistry.registerFluid(SLURRY);
    }
}