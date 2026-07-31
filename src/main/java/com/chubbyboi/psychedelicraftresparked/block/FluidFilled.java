package com.chubbyboi.psychedelicraftresparked.block;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface FluidFilled {
    @Nullable
    AxisAlignedBB getFluidBoundingBox(World world, BlockPos pos);

    int getFluidTintColor(World world, BlockPos pos);
}