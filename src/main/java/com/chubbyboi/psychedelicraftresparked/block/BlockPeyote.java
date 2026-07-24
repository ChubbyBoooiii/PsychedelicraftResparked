package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityPeyote;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockPeyote extends BlockBush implements IGrowable, ITileEntityProvider {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);

    public BlockPeyote(String name) {
        super(Material.PLANTS);
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);

        setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0));

        BlockInit.BLOCKS.add(this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(AGE, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityPeyote();
    }

    // Matches source's permissive canPlaceBlockOn(Block) { return block.isNormalCube(); } - Peyote
    // can be planted/stay on any solid ground, not restricted to EnumPlantType.Desert-compatible soil.
    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        IBlockState soil = worldIn.getBlockState(pos.down());
        return soil.isNormalCube();
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return state.isNormalCube();
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Desert;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int age = state.getValue(AGE);
        for (int i = 0; i <= age; i++) {
            drops.add(new ItemStack(this));
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);

        int age = state.getValue(AGE);
        int chance = age < 3 ? 20 : 120;
        if (rand.nextInt(chance) == 0) {
            growStep(worldIn, rand, pos, state);
        }
    }

    private void growStep(World world, Random random, BlockPos pos, IBlockState state) {
        int age = state.getValue(AGE);

        if (age < 3) {
            world.setBlockState(pos, state.withProperty(AGE, age + 1), 2);
            return;
        }

        BlockPos candidate = pos.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        BlockPos anchor = pos;

        for (int i = 0; i < 4; i++) {
            if (world.isAirBlock(candidate) && canPlaceBlockAt(world, candidate)) {
                anchor = candidate;
            }
            candidate = anchor.add(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        }

        if (world.isAirBlock(candidate) && canPlaceBlockAt(world, candidate)) {
            world.setBlockState(candidate, getDefaultState(), 3);
        }
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        growStep(worldIn, rand, pos, state);
    }
}