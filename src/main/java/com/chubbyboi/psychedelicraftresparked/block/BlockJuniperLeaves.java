package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockJuniperLeaves extends BlockLeaves implements IGrowable {
    public static final PropertyBool BERRIES = PropertyBool.create("berries");

    public BlockJuniperLeaves(String name) {
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);
        setDefaultState(this.blockState.getBaseState()
            .withProperty(BERRIES, false)
            .withProperty(DECAYABLE, true)
            .withProperty(CHECK_DECAY, true));
        BlockInit.BLOCKS.add(this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BERRIES, DECAYABLE, CHECK_DECAY);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
            .withProperty(BERRIES, (meta & 1) != 0)
            .withProperty(DECAYABLE, (meta & 2) == 0)
            .withProperty(CHECK_DECAY, (meta & 4) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if (state.getValue(BERRIES)) meta |= 1;
        if (!state.getValue(DECAYABLE)) meta |= 2;
        if (state.getValue(CHECK_DECAY)) meta |= 4;
        return meta;
    }

    @Override
    public BlockPlanks.EnumType getWoodType(int meta) {
        return BlockPlanks.EnumType.OAK;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(BlockInit.JUNIPER_SAPLING);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
        if (state.getValue(BERRIES)) {
            drops.add(new ItemStack(ItemInit.JUNIPER_BERRIES));
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote && !state.getValue(BERRIES) && rand.nextFloat() < 0.01F) {
            world.setBlockState(pos, state.withProperty(BERRIES, true), 4);
        }
        super.updateTick(world, pos, state, rand);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (state.getValue(BERRIES)) {
            if (!world.isRemote) {
                spawnAsEntity(world, pos, new ItemStack(ItemInit.JUNIPER_BERRIES));
                world.setBlockState(pos, state.withProperty(BERRIES, false), 4);
            }
            return true;
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return NonNullList.withSize(1, new ItemStack(this, 1, getMetaFromState(world.getBlockState(pos))));
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return !state.getValue(BERRIES);
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return canGrow(world, pos, state, false);
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state.withProperty(BERRIES, true), 4);
    }
}