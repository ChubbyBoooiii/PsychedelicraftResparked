package com.chubbyboi.psychedelicraftresparked.worldgen;

import com.chubbyboi.psychedelicraftresparked.block.BlockJuniperLeaves;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;

public class JuniperTreeGenerator extends WorldGenAbstractTree {
    private static final IBlockState LEAVES = BlockInit.JUNIPER_LEAVES.getDefaultState()
        .withProperty(BlockJuniperLeaves.CHECK_DECAY, false);
    private static final IBlockState LEAVES_RIPE = LEAVES.withProperty(BlockJuniperLeaves.BERRIES, true);

    // Only naturally world-generated trees (not player-grown saplings) spawn with any ripe leaves already.
    private static final float RIPE_LEAF_CHANCE = 0.05F;

    private final boolean naturallyGenerated;

    public JuniperTreeGenerator(boolean notify) {
        super(notify);
        this.naturallyGenerated = !notify;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos position) {
        int height = rand.nextInt(3) + rand.nextInt(3) + 5;

        if (position.getY() < 1 || position.getY() + height + 1 > 256) {
            return false;
        }

        boolean canPlace = true;
        for (int y = position.getY(); y <= position.getY() + 1 + height && canPlace; y++) {
            int radius = 1;
            if (y == position.getY()) {
                radius = 0;
            }
            if (y >= position.getY() + 1 + height - 2) {
                radius = 2;
            }

            for (int x = position.getX() - radius; x <= position.getX() + radius && canPlace; x++) {
                for (int z = position.getZ() - radius; z <= position.getZ() + radius && canPlace; z++) {
                    if (y < 0 || y >= 256 || !isReplaceable(world, new BlockPos(x, y, z))) {
                        canPlace = false;
                    }
                }
            }
        }

        if (!canPlace) {
            return false;
        }

        BlockPos down = position.down();
        IBlockState soil = world.getBlockState(down);
        boolean isSoil = soil.getBlock().canSustainPlant(soil, world, down, EnumFacing.UP, (BlockSapling) Blocks.SAPLING);

        if (!isSoil || position.getY() >= world.getHeight() - height - 1) {
            return false;
        }

        soil.getBlock().onPlantGrow(soil, world, down, position);

        EnumFacing facing = EnumFacing.Plane.HORIZONTAL.random(rand);
        int leanStart = height - rand.nextInt(4) - 1;
        int leanSteps = 3 - rand.nextInt(3);
        int x = position.getX();
        int z = position.getZ();
        int topY = 0;

        for (int i = 0; i < height; i++) {
            int y = position.getY() + i;

            if (i >= leanStart && leanSteps > 0) {
                x += facing.getXOffset();
                z += facing.getZOffset();
                leanSteps--;
            }

            BlockPos logPos = new BlockPos(x, y, z);
            IBlockState state = world.getBlockState(logPos);
            if (state.getBlock().isAir(state, world, logPos) || state.getBlock().isLeaves(state, world, logPos)) {
                setBlockAndNotifyAdequately(world, logPos, BlockInit.JUNIPER_LOG.getDefaultState());
                topY = y;
            }
        }

        BlockPos canopyBase = new BlockPos(x, topY, z);

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) != 3 || Math.abs(dz) != 3) {
                    placeLeafAt(world, rand, canopyBase.add(dx, 0, dz));
                }
            }
        }

        BlockPos canopyTop = canopyBase.up();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                placeLeafAt(world, rand, canopyTop.add(dx, 0, dz));
            }
        }

        placeLeafAt(world, rand, canopyTop.east(2));
        placeLeafAt(world, rand, canopyTop.west(2));
        placeLeafAt(world, rand, canopyTop.south(2));
        placeLeafAt(world, rand, canopyTop.north(2));

        x = position.getX();
        z = position.getZ();
        EnumFacing branchFacing = EnumFacing.Plane.HORIZONTAL.random(rand);

        if (branchFacing != facing) {
            int branchStart = leanStart - rand.nextInt(2) - 1;
            int branchLength = 1 + rand.nextInt(3);
            topY = 0;

            for (int i = branchStart; i < height && branchLength > 0; i++, branchLength--) {
                if (i >= 1) {
                    int y = position.getY() + i;
                    x += branchFacing.getXOffset();
                    z += branchFacing.getZOffset();

                    BlockPos logPos = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(logPos);
                    if (state.getBlock().isAir(state, world, logPos) || state.getBlock().isLeaves(state, world, logPos)) {
                        setBlockAndNotifyAdequately(world, logPos, BlockInit.JUNIPER_LOG.getDefaultState());
                        topY = y;
                    }
                }
            }

            if (topY > 0) {
                BlockPos branchBase = new BlockPos(x, topY, z);

                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        if (Math.abs(dx) != 2 || Math.abs(dz) != 2) {
                            placeLeafAt(world, rand, branchBase.add(dx, 0, dz));
                        }
                    }
                }

                BlockPos branchTop = branchBase.up();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        placeLeafAt(world, rand, branchTop.add(dx, 0, dz));
                    }
                }
            }
        }

        return true;
    }

    private void placeLeafAt(World world, Random rand, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock().isAir(state, world, pos) || state.getBlock().isLeaves(state, world, pos)) {
            boolean ripe = naturallyGenerated && rand.nextFloat() < RIPE_LEAF_CHANCE;
            setBlockAndNotifyAdequately(world, pos, ripe ? LEAVES_RIPE : LEAVES);
        }
    }
}
