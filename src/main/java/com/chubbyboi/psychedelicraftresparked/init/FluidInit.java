package com.chubbyboi.psychedelicraftresparked.init;

import com.chubbyboi.psychedelicraftresparked.fluids.FluidDrug;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidInit {

    public static final FluidDrug COFFEE = new FluidDrug("coffee",
        new ResourceLocation("minecraft:blocks/water_still"),
        new ResourceLocation("minecraft:blocks/water_flow")
    );

    static {
        COFFEE.setDrinkable(true);
        COFFEE.setColor(0xffa77d55);
        COFFEE.addDrugInfluencePerBucket("caffeine", 20, 0.002, 0.001, 0.25);
    }

    public static void registerFluids() {
        FluidRegistry.registerFluid(COFFEE);
    }
}
