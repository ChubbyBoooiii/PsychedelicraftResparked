package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDistillery;
import com.chubbyboi.psychedelicraftresparked.util.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.Random;

public class BlockDistillery extends Block implements ITileEntityProvider {

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.9, 0.75);

    public BlockDistillery(String name) {
        super(Material.GLASS);
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);
        setHardness(0.3F);
        setSoundType(SoundType.GLASS);
        BlockInit.BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityDistillery();
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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(PsychedelicraftResparked.instance, GuiHandler.DISTILLERY_ID, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityDistillery) {
            InventoryHelper.dropInventoryItems(world, pos, (TileEntityDistillery) tileEntity);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityDistillery) {
            TileEntityDistillery distillery = (TileEntityDistillery) tileEntity;

            distillery.setRotation((MathHelper.floor((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) + 1) & 3);

            IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (handler != null) {
                FluidStack fluid = handler.drain(TileEntityDistillery.CAPACITY, false);
                if (fluid != null && fluid.amount > 0) {
                    distillery.getTank().fill(fluid, true);
                }
            }
        }
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (willHarvest) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityDistillery) {
                spawnAsEntity(world, pos, createFilledStack((TileEntityDistillery) tileEntity));
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity instanceof TileEntityDistillery ? createFilledStack((TileEntityDistillery) tileEntity) : new ItemStack(ItemInit.DISTILLERY_ITEM);
    }

    private ItemStack createFilledStack(TileEntityDistillery tileEntity) {
        ItemStack stack = new ItemStack(ItemInit.DISTILLERY_ITEM);
        FluidStack fluid = tileEntity.getTank().getFluid();
        if (fluid != null && fluid.amount > 0) {
            IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (handler != null) {
                handler.fill(fluid, true);
            }
        }
        return stack;
    }
}