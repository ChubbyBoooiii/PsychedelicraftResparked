package com.chubbyboi.psychedelicraftresparked.worldgen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GeneratorGeneric implements IWorldGenerator {
    private final WorldGenerator generator;
    private final List<Entry> biomeEntries;

    public GeneratorGeneric(WorldGenerator generator, Entry... biomeEntries) {
        this.generator = generator;
        this.biomeEntries = Arrays.asList(biomeEntries);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        Biome biome = world.getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));

        int generate = -1;
        for (Entry entry : biomeEntries) {
            generate = entry.numberGenerated(random, biome);
            if (generate >= 0) {
                break;
            }
        }

        for (int i = 0; i < generate; i++) {
            int genX = chunkX * 16 + random.nextInt(16) + 8;
            int genZ = chunkZ * 16 + random.nextInt(16) + 8;
            BlockPos genPos = world.getHeight(new BlockPos(genX, 0, genZ));

            generator.generate(world, random, genPos);
        }
    }

    public interface Entry {
        int numberGenerated(Random random, Biome biome);
    }

    public static class EntryBiomeTypes implements Entry {
        private final BiomeDictionary.Type[] types;
        private final float chance;
        private final int number;

        public EntryBiomeTypes(float chance, int number, BiomeDictionary.Type... types) {
            this.chance = chance;
            this.number = number;
            this.types = types;
        }

        @Override
        public int numberGenerated(Random random, Biome biome) {
            for (BiomeDictionary.Type type : types) {
                if (!BiomeDictionary.hasType(biome, type)) {
                    return -1;
                }
            }

            return random.nextFloat() < chance ? number : 0;
        }
    }
}