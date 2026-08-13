package com.chubbyboi.psychedelicraftresparked.worldgen;

import com.chubbyboi.psychedelicraftresparked.block.BlockTallCrop;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class WorldGenWildCrop extends WorldGenerator {
    private final BlockTallCrop block;

    public WorldGenWildCrop(boolean notify, BlockTallCrop block) {
        super(notify);
        this.block = block;
    }

    @Override
    public boolean generate(World world, Random random, BlockPos position) {
        if (!world.isAirBlock(position) || world.getBlockState(position.down()).getBlock() != Blocks.GRASS) {
            return false;
        }

        int range = random.nextInt(3) + 1;

        for (int xPlus = -range; xPlus <= range; xPlus++) {
            for (int zPlus = -range; zPlus <= range; zPlus++) {
                if (xPlus * xPlus + zPlus * zPlus >= range * range) {
                    continue;
                }

                BlockPos columnBase = position.add(xPlus, 0, zPlus);
                if (!world.isAirBlock(columnBase) || world.getBlockState(columnBase.down()).getBlock() != Blocks.GRASS) {
                    continue;
                }

                if (random.nextInt(3) != 0) {
                    continue;
                }

                int height = block.getMaxHeight();
                IBlockState grown = block.getDefaultState().withProperty(block.getAgeProperty(), block.getMaxAge());

                for (int h = 0; h < height; h++) {
                    BlockPos plantPos = columnBase.up(h);
                    if (block.canBlockStay(world, plantPos, grown)) {
                        setBlockAndNotifyAdequately(world, plantPos, grown);
                    }
                }
            }
        }

        return true;
    }
}