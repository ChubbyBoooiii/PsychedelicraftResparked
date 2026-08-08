package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.item.ItemRiftJar;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityRiftJar;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockRiftJar extends Block implements ITileEntityProvider {

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.1, 0.1, 0.1, 0.9, 0.8, 0.9);

    public BlockRiftJar(String name) {
        super(Material.GLASS);
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);
        setSoundType(SoundType.STONE);
        BlockInit.BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityRiftJar();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
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
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityRiftJar) {
            TileEntityRiftJar jar = (TileEntityRiftJar) tileEntity;
            jar.blockRotation = placer.getHorizontalFacing().getOpposite().getHorizontalIndex();
            jar.currentRiftFraction = ItemRiftJar.getRiftFraction(stack);

            if (!world.isRemote) {
                jar.markDirty();
                world.notifyBlockUpdate(pos, state, state, 3);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityRiftJar) {
            TileEntityRiftJar jar = (TileEntityRiftJar) tileEntity;

            if (player.isSneaking()) {
                jar.toggleSuckingRifts();
            } else {
                jar.toggleRiftJarOpen();
            }
            return true;
        }
        return false;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (willHarvest) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityRiftJar) {
                TileEntityRiftJar jar = (TileEntityRiftJar) tileEntity;
                if (!jar.jarBroken) {
                    spawnAsEntity(world, pos, ItemRiftJar.createFilledRiftJar(jar.currentRiftFraction, ItemInit.RIFT_JAR_ITEM));
                }
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity instanceof TileEntityRiftJar
            ? ItemRiftJar.createFilledRiftJar(((TileEntityRiftJar) tileEntity).currentRiftFraction, ItemInit.RIFT_JAR_ITEM)
            : new ItemStack(ItemInit.RIFT_JAR_ITEM);
    }
}