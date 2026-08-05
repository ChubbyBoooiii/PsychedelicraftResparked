package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockLattice extends Block implements IGrowable {
    public static final PropertyInteger GROWTH = PropertyInteger.create("growth", 0, 4);
    public static final PropertyBool AXIS_X = PropertyBool.create("axis_x");

    private static final AxisAlignedBB ALONG_X = new AxisAlignedBB(0.0, 0.0, 0.4, 1.0, 1.0, 0.6);
    private static final AxisAlignedBB ALONG_Z = new AxisAlignedBB(0.4, 0.0, 0.0, 0.6, 1.0, 1.0);

    public BlockLattice(String name) {
        super(Material.WOOD);
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);
        setHardness(0.3F);
        setTickRandomly(true);
        setDefaultState(this.blockState.getBaseState().withProperty(GROWTH, 0).withProperty(AXIS_X, true));
        BlockInit.BLOCKS.add(this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, GROWTH, AXIS_X);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
            .withProperty(AXIS_X, (meta & 1) == 0)
            .withProperty(GROWTH, meta >> 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(AXIS_X) ? 0 : 1) | (state.getValue(GROWTH) << 1);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(AXIS_X) ? ALONG_X : ALONG_Z;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, @Nullable EntityLivingBase placer, ItemStack stack) {
        boolean axisX = true;
        if (placer != null) {
            int dir = MathHelper.floor((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            axisX = dir == 0 || dir == 2;
        }
        world.setBlockState(pos, state.withProperty(AXIS_X, axisX), 3);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) {
            return;
        }

        int growth = state.getValue(GROWTH);
        if (growth > 0 && growth < 4 && world.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(35) == 0) {
            world.setBlockState(pos, state.withProperty(GROWTH, growth + 1), 2);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (state.getValue(GROWTH) != 4) {
            return false;
        }

        if (!world.isRemote) {
            int count = 1 + world.rand.nextInt(3);
            spawnAsEntity(world, pos, new ItemStack(ItemInit.GRAPES, count));
            world.setBlockState(pos, state.withProperty(GROWTH, 2), 2);
        }
        return true;
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        int growth = state.getValue(GROWTH);
        return growth > 0 && growth < 4;
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return canGrow(world, pos, state, false);
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        int growth = state.getValue(GROWTH);
        if (growth > 0 && growth < 4 && rand.nextInt(2) == 0) {
            world.setBlockState(pos, state.withProperty(GROWTH, growth + 1), 2);
        }
    }

    public boolean tryPlant(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(GROWTH) != 0) {
            return false;
        }
        if (!world.isRemote) {
            world.setBlockState(pos, state.withProperty(GROWTH, 1), 2);
        }
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(new ItemStack(this));
        if (state.getValue(GROWTH) == 4) {
            drops.add(new ItemStack(ItemInit.GRAPES, 1 + new Random().nextInt(3)));
        }
    }
}