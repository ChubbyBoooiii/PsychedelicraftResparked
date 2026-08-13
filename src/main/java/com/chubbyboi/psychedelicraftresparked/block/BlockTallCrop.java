package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Blocks;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockTallCrop extends BlockCrops {
    private static final AxisAlignedBB TALL_CROP_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D);
    private static final PropertyInteger UNIVERSAL_AGE = PropertyInteger.create("age", 0, 15);
    public static final PropertyBool TOP = PropertyBool.create("top");

    private Item cropSeed;
    private final List<CropDrop> cropDrops = new ArrayList<>();
    private final int maxAge;
    private final int maxHeight;

    public static class CropDrop {
        private final Item item;
        private final int minAmount;
        private final int maxAmount;

        public CropDrop(Item item, int minAmount, int maxAmount) {
            this.item = item;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }

        public Item getItem() {
            return item;
        }

        public int getRandomAmount(Random rand) {
            if (minAmount == maxAmount) {
                return minAmount;
            }
            return minAmount + rand.nextInt(maxAmount - minAmount + 1);
        }
    }

    public BlockTallCrop(String name, int maxAge, int maxHeight) {
        super();
        setTranslationKey(name);
        setRegistryName(name);

        this.maxAge = maxAge;
        this.maxHeight = maxHeight;

        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(this.getAgeProperty(), 0)
                .withProperty(TOP, false)
        );

        BlockInit.BLOCKS.add(this);
    }

    public void setSeedItem(Item seed) {
        this.cropSeed = seed;
    }

    public void addCropDrop(Item item, int minAmount, int maxAmount) {
        cropDrops.add(new CropDrop(item, minAmount, maxAmount));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return TALL_CROP_AABB;
    }

    @Override
    protected Item getSeed() {
        return cropSeed != null ? cropSeed : super.getSeed();
    }

    @Override
    public int getMaxAge() {
        return this.maxAge;
    }

    public int getMaxHeight() {
        return this.maxHeight;
    }

    @Override
    public PropertyInteger getAgeProperty() {
        return UNIVERSAL_AGE;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, UNIVERSAL_AGE, TOP);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(this.getAgeProperty(), meta)
            .withProperty(TOP, false);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(this.getAgeProperty());
    }

    @Override
    public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World)world).rand : new Random();
        int age = getAge(state);

        BlockPos belowPos = pos.down();
        IBlockState below = world.getBlockState(belowPos);
        boolean isBase = below.getBlock().canSustainPlant(below, world, belowPos, EnumFacing.UP, this);

        if (isBase) {
            drops.add(new ItemStack(this.getSeed(), 1, 0));
        } else {
            if (age >= getMaxAge() && rand.nextFloat() < 0.50F) {
                drops.add(new ItemStack(this.getSeed(), 1, 0));
            }
        }

        if (age >= getMaxAge()) {
            for (CropDrop cropDrop : cropDrops) {
                int amount = cropDrop.getRandomAmount(rand);
                if (amount > 0) {
                    drops.add(new ItemStack(cropDrop.getItem(), amount));
                }
            }
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        IBlockState aboveState = worldIn.getBlockState(pos.up());

        if (aboveState.getBlock() != this) {
            World world = worldIn instanceof World ? (World) worldIn : null;

            if (world != null) {
                int currentHeight = getCurrentHeight(world, pos);
                return state.withProperty(TOP, currentHeight >= this.maxHeight);
            } else {
                // ChunkCache fallback
                int currentHeight = 1;
                BlockPos checkPos = pos.down();

                for (int i = 0; i < this.maxHeight; i++) {
                    if (worldIn.getBlockState(checkPos).getBlock() == this) {
                        currentHeight++;
                        checkPos = checkPos.down();
                    } else {
                        break;
                    }
                }
                return state.withProperty(TOP, currentHeight >= this.maxHeight);
            }
        }

        return state.withProperty(TOP, false);
    }

    private int getCurrentHeight(World world, BlockPos pos) {
        int height = 1;
        BlockPos checkPos = pos.down();

        while (world.getBlockState(checkPos).getBlock() == this) {
            height++;
            checkPos = checkPos.down();
        }
        return height;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) {
            return;
        }

        if (world.getLightFromNeighbors(pos.up()) >= 9) {
            int currentAge = this.getAge(state);

            if (currentAge < this.getMaxAge()) {
                float growthSpeed = getGrowthChance(this, world, pos);
                if (rand.nextInt((int)(25.0F / growthSpeed) + 1) == 0) {
                    world.setBlockState(pos, this.withAge(currentAge + 1), 2);
                }
            } else {
                int currentHeight = getCurrentHeight(world, pos);

                if (currentHeight < this.maxHeight) {
                    BlockPos abovePos = pos.up();
                    if (world.isAirBlock(abovePos)) {
                        world.setBlockState(abovePos, this.getDefaultState().withProperty(this.getAgeProperty(), 0), 2);
                    }
                }
            }
        }
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        int currentAge = this.getAge(state);

        if (currentAge < this.getMaxAge()) {
            return true;
        }

        BlockPos abovePos = pos.up();
        IBlockState aboveState = world.getBlockState(abovePos);

        if (aboveState.getBlock() == this) {
            return canGrow(world, abovePos, aboveState, isClient);
        } else if (world.isAirBlock(abovePos)) {
            BlockPos topPos = pos;
            while (world.getBlockState(topPos.up()).getBlock() == this) {
                topPos = topPos.up();
            }

            int currentHeight = getCurrentHeight(world, topPos);
            return currentHeight < this.maxHeight;
        }

        return false;
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return canGrow(world, pos, state, false);
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        int currentAge = this.getAge(state);

        if (currentAge < this.getMaxAge()) {
            int newAge = currentAge + this.getBonemealAgeIncrease(world);
            if (newAge > this.getMaxAge()) {
                newAge = this.getMaxAge();
            }
            world.setBlockState(pos, this.withAge(newAge), 2);
        } else {
            BlockPos abovePos = pos.up();
            IBlockState aboveState = world.getBlockState(abovePos);

            if (aboveState.getBlock() == this) {
                this.grow(world, rand, abovePos, aboveState);
            } else if (world.isAirBlock(abovePos)) {
                BlockPos topPos = pos;
                while (world.getBlockState(topPos.up()).getBlock() == this) {
                    topPos = topPos.up();
                }

                int currentHeight = getCurrentHeight(world, topPos);

                if (currentHeight < this.maxHeight) {
                    world.setBlockState(abovePos, this.getDefaultState().withProperty(this.getAgeProperty(), 0), 2);
                }
            }
        }
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        Block belowBlock = world.getBlockState(pos.down()).getBlock();

        return belowBlock == this || belowBlock == Blocks.FARMLAND || belowBlock == Blocks.GRASS;
    }
}