package com.chubbyboi.psychedelicraftresparked.worldgen;

import com.chubbyboi.psychedelicraftresparked.config.PSConfig;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.worldgen.GeneratorGeneric.EntryBiomeTypes;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class PSWorldGen {
    public static void initWorldGen() {
        if (PSConfig.generateJuniper) {
            GameRegistry.registerWorldGenerator(new GeneratorGeneric(new JuniperTreeGenerator(false),
                new EntryBiomeTypes(0.1f, 1, Type.HILLS, Type.COLD),
                new EntryBiomeTypes(0.05f, 1, Type.FOREST, Type.COLD),
                new EntryBiomeTypes(0.05f, 1, Type.SNOWY, Type.WASTELAND)
            ), 10);
        }

        if (PSConfig.generateCannabis) {
            GameRegistry.registerWorldGenerator(new GeneratorGeneric(new WorldGenWildCrop(false, BlockInit.CANNABIS_PLANT),
                new EntryBiomeTypes(0.0f, 1, Type.COLD),
                new EntryBiomeTypes(0.0f, 1, Type.HILLS),
                new EntryBiomeTypes(0.04f, 1, Type.PLAINS),
                new EntryBiomeTypes(0.04f, 1, Type.FOREST)
            ), 10);
        }

        if (PSConfig.generateHop) {
            GameRegistry.registerWorldGenerator(new GeneratorGeneric(new WorldGenWildCrop(false, BlockInit.HOPS_PLANT),
                new EntryBiomeTypes(0.0f, 1, Type.COLD),
                new EntryBiomeTypes(0.0f, 1, Type.HILLS),
                new EntryBiomeTypes(0.06f, 1, Type.PLAINS),
                new EntryBiomeTypes(0.06f, 1, Type.FOREST)
            ), 10);
        }

        if (PSConfig.generateTobacco) {
            GameRegistry.registerWorldGenerator(new GeneratorGeneric(new WorldGenWildCrop(false, BlockInit.TOBACCO_PLANT),
                new EntryBiomeTypes(0.0f, 1, Type.COLD),
                new EntryBiomeTypes(0.0f, 1, Type.HILLS),
                new EntryBiomeTypes(0.04f, 1, Type.PLAINS),
                new EntryBiomeTypes(0.04f, 1, Type.FOREST)
            ), 10);
        }

        if (PSConfig.generateCoffea) {
            GameRegistry.registerWorldGenerator(new GeneratorGeneric(new WorldGenWildCrop(false, BlockInit.COFFEA_PLANT),
                new EntryBiomeTypes(0.0f, 1, Type.COLD),
                new EntryBiomeTypes(0.0f, 1, Type.HILLS),
                new EntryBiomeTypes(0.05f, 1, Type.PLAINS),
                new EntryBiomeTypes(0.05f, 1, Type.FOREST)
            ), 10);
        }

        if (PSConfig.generateCoca) {
            GameRegistry.registerWorldGenerator(new GeneratorGeneric(new WorldGenWildCrop(false, BlockInit.COCA_PLANT),
                new EntryBiomeTypes(0.0f, 1, Type.COLD),
                new EntryBiomeTypes(0.0f, 1, Type.HILLS),
                new EntryBiomeTypes(0.0f, 1, Type.SPARSE),
                new EntryBiomeTypes(0.02f, 1, Type.PLAINS),
                new EntryBiomeTypes(0.02f, 1, Type.FOREST)
            ), 10);
        }

        if (PSConfig.generatePeyote) {
            GameRegistry.registerWorldGenerator(new GeneratorGeneric(new WorldGenPeyote(false),
                new EntryBiomeTypes(0.04f, 1, Type.SANDY, Type.HOT),
                new EntryBiomeTypes(0.04f, 1, Type.MOUNTAIN, Type.HOT)
            ), 10);
        }
    }
}