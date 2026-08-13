package com.chubbyboi.psychedelicraftresparked.worldgen;

import com.chubbyboi.psychedelicraftresparked.block.BlockPeyote;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class WorldGenPeyote extends WorldGenerator {
    public WorldGenPeyote(boolean notify) {
        super(notify);
    }

    @Override
    public boolean generate(World world, Random random, BlockPos position) {
        if (world.isAirBlock(position) && BlockInit.PEYOTE_PLANT.canPlaceBlockAt(world, position)) {
            IBlockState state = BlockInit.PEYOTE_PLANT.getDefaultState()
                .withProperty(BlockPeyote.AGE, 3);
            setBlockAndNotifyAdequately(world, position, state);
        }

        return true;
    }
}