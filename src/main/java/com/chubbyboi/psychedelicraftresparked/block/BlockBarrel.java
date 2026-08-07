package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.item.ItemBarrel;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBarrel;
import com.chubbyboi.psychedelicraftresparked.util.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.Random;

public class BlockBarrel extends Block implements ITileEntityProvider {

    public BlockBarrel(String name) {
        super(Material.WOOD);
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);
        setHardness(2.0F);
        setSoundType(SoundType.WOOD);
        BlockInit.BLOCKS.add(this);
    }

    public static final PropertyEnum<BlockPlanks.EnumType> WOOD_TYPE = PropertyEnum.create("wood", BlockPlanks.EnumType.class);

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, WOOD_TYPE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(WOOD_TYPE).getMetadata();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(WOOD_TYPE, BlockPlanks.EnumType.byMetadata(meta));
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityBarrel();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntityBarrel)) {
            return false;
        }
        TileEntityBarrel barrel = (TileEntityBarrel) tileEntity;

        ItemStack heldItem = player.getHeldItem(hand);

        if (heldItem.getItem() == ItemInit.TAP) {
            if (!barrel.hasTap()) {
                if (!world.isRemote) {
                    barrel.attachTap();
                    heldItem.shrink(1);
                    world.notifyBlockUpdate(pos, state, state, 3);
                }
                return true;
            }
        } else if (!world.isRemote && barrel.hasTap() && tryPour(world, pos, state, player, hand, barrel, heldItem)) {
            return true;
        }

        if (!world.isRemote) {
            player.openGui(PsychedelicraftResparked.instance, GuiHandler.BARREL_ID, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    private boolean tryPour(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, TileEntityBarrel barrel, ItemStack heldItem) {
        if (heldItem.isEmpty() || heldItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) == null) {
            return false;
        }

        boolean split = heldItem.getCount() > 1;
        ItemStack stack = split ? heldItem.splitStack(1) : heldItem;

        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        boolean filled = handler != null && pourIntoSingle(barrel, handler);

        if (split) {
            if (!player.inventory.addItemStackToInventory(stack)) {
                world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, stack));
            }
        } else {
            player.setHeldItem(hand, stack);
        }

        if (filled) {
            barrel.openTap();
            world.notifyBlockUpdate(pos, state, state, 3);
        }
        return filled;
    }

    private boolean pourIntoSingle(TileEntityBarrel barrel, IFluidHandlerItem handler) {
        FluidStack simulated = barrel.getTank().drain(FluidHelper.BUCKET_VOLUME, false);
        if (simulated == null || simulated.amount <= 0) {
            return false;
        }
        int accepted = handler.fill(simulated, false);
        if (accepted <= 0) {
            return false;
        }
        FluidStack drained = barrel.getTank().drain(accepted, true);
        handler.fill(drained, true);
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityBarrel) {
            TileEntityBarrel barrel = (TileEntityBarrel) tileEntity;

            barrel.setRotation(MathHelper.floor((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);

            IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (handler != null) {
                FluidStack fluid = handler.drain(TileEntityBarrel.CAPACITY, false);
                if (fluid != null && fluid.amount > 0) {
                    barrel.getTank().fill(fluid, true);
                }
            }

            if (ItemBarrel.isSealed(stack)) {
                barrel.setSealed(true);
            }
            if (ItemBarrel.hasTap(stack)) {
                barrel.attachTap();
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
            if (tileEntity instanceof TileEntityBarrel) {
                TileEntityBarrel barrel = (TileEntityBarrel) tileEntity;
                ItemStack barrelStack = createFilledStack(barrel, getMetaFromState(state));

                if (barrel.hasTap()) {
                    ItemBarrel.setHasTap(barrelStack, false);
                    spawnAsEntity(world, pos, new ItemStack(ItemInit.TAP));
                }

                spawnAsEntity(world, pos, barrelStack);
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity instanceof TileEntityBarrel ? createFilledStack((TileEntityBarrel) tileEntity, getMetaFromState(state)) : new ItemStack(ItemInit.BARREL_ITEM);
    }

    private ItemStack createFilledStack(TileEntityBarrel tileEntity, int woodMeta) {
        ItemStack stack = new ItemStack(ItemInit.BARREL_ITEM, 1, woodMeta);
        FluidStack fluid = tileEntity.getTank().getFluid();
        if (fluid != null && fluid.amount > 0) {
            IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (handler != null) {
                handler.fill(fluid, true);
            }
        }

        if (tileEntity.isSealed()) {
            ItemBarrel.setSealed(stack, true);
        }
        if (tileEntity.hasTap()) {
            ItemBarrel.setHasTap(stack, true);
        }
        return stack;
    }
}