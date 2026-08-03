package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVatCompanion;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockVatCompanion extends Block implements ITileEntityProvider, FluidFilled {
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

    public BlockVatCompanion(String name) {
        super(Material.WOOD);
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);
        setHardness(2.0F);
        setSoundType(SoundType.WOOD);
        BlockInit.BLOCKS.add(this);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityVatCompanion();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntityVatCompanion)) {
            return false;
        }
        BlockPos masterPos = ((TileEntityVatCompanion) tileEntity).getMasterPos();
        if (masterPos == null || world.getBlockState(masterPos).getBlock() != BlockInit.VAT) {
            return false;
        }
        return BlockInit.VAT.onBlockActivated(world, masterPos, world.getBlockState(masterPos), player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityVatCompanion) {
            BlockPos masterPos = ((TileEntityVatCompanion) tileEntity).getMasterPos();
            if (masterPos != null && world.getBlockState(masterPos).getBlock() == BlockInit.VAT) {
                world.destroyBlock(masterPos, true);
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(ItemInit.VAT);
    }

    @Nullable
    @Override
    public net.minecraft.item.Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ItemInit.VAT;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        BlockPos masterPos = tileEntity instanceof TileEntityVatCompanion ? ((TileEntityVatCompanion) tileEntity).getMasterPos() : null;
        if (masterPos != null) {
            BlockVat.addVatCollisionBoxes(worldIn, masterPos, entityBox, collidingBoxes);
        } else {
            Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, SHAPE);
        }
    }

    @Override
    @Nullable
    public AxisAlignedBB getFluidBoundingBox(World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        BlockPos masterPos = tileEntity instanceof TileEntityVatCompanion ? ((TileEntityVatCompanion) tileEntity).getMasterPos() : null;
        return masterPos != null ? BlockVat.getVatFluidBoundingBox(world, masterPos) : null;
    }

    @Override
    public int getFluidTintColor(World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        BlockPos masterPos = tileEntity instanceof TileEntityVatCompanion ? ((TileEntityVatCompanion) tileEntity).getMasterPos() : null;
        return masterPos != null ? BlockVat.getVatFluidTintColor(world, masterPos) : 0xFFFFFFFF;
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
}