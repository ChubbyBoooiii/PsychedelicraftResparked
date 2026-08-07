package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.fluids.FluidHelper;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import com.chubbyboi.psychedelicraftresparked.util.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockVat extends Block implements ITileEntityProvider, FluidFilled {
    public static final float BASIN_MIN_XZ = 0.044194F;
    public static final float BASIN_MAX_XZ = 1.955806F;
    public static final float BASIN_FLOOR_Y = 0.09F;
    public static final float BASIN_RIM_Y = 0.95F;

    public BlockVat(String name) {
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
        return EnumBlockRenderType.MODEL;
    }

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyEnum<BlockPlanks.EnumType> WOOD_TYPE = PropertyEnum.create("wood", BlockPlanks.EnumType.class);

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, WOOD_TYPE);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        EnumFacing primary = tileEntity instanceof TileEntityVat ? ((TileEntityVat) tileEntity).getPrimaryDirection() : null;
        return state.withProperty(FACING, primary != null ? primary : EnumFacing.EAST);
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
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        addVatCollisionBoxes(worldIn, pos, entityBox, collidingBoxes);
    }

    public static void addVatCollisionBoxes(World world, BlockPos masterPos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes) {
        TileEntity tileEntity = world.getTileEntity(masterPos);
        EnumFacing primary = tileEntity instanceof TileEntityVat ? ((TileEntityVat) tileEntity).getPrimaryDirection() : null;

        float border = 0.0442F;
        float floorHeight = 0.0625F;
        float height = 1.0F;

        float[][] localBoxes = {
            {0.0F, 0.0F, 0.0F, border, height, 2.0F},               // -X wall
            {2.0F - border, 0.0F, 0.0F, 2.0F, height, 2.0F},        // +X wall
            {0.0F, 0.0F, 0.0F, 2.0F, height, border},                // -Z wall
            {0.0F, 0.0F, 2.0F - border, 2.0F, height, 2.0F},        // +Z wall
            {0.0F, 0.0F, 0.0F, 2.0F, floorHeight, 2.0F},             // floor
        };

        for (float[] b : localBoxes) {
            AxisAlignedBB box = rotateLocalBox(masterPos, primary, b[0], b[1], b[2], b[3], b[4], b[5]);
            if (box.intersects(entityBox)) {
                collidingBoxes.add(box);
            }
        }
    }

    private static AxisAlignedBB rotateLocalBox(BlockPos masterPos, EnumFacing primary, float x0, float y0, float z0, float x1, float y1, float z1) {
        double angle = Math.toRadians(TileEntityVat.rotationFor(primary));
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double[][] corners = {{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}};
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (double[] corner : corners) {
            double rx = corner[0] - 0.5;
            double rz = corner[1] - 0.5;
            double wx = masterPos.getX() + 0.5 + (rx * cos + rz * sin);
            double wz = masterPos.getZ() + 0.5 + (-rx * sin + rz * cos);
            minX = Math.min(minX, wx);
            maxX = Math.max(maxX, wx);
            minZ = Math.min(minZ, wz);
            maxZ = Math.max(maxZ, wz);
        }

        return new AxisAlignedBB(minX, masterPos.getY() + y0, minZ, maxX, masterPos.getY() + y1, maxZ);
    }

    @Override
    @Nullable
    public AxisAlignedBB getFluidBoundingBox(World world, BlockPos pos) {
        return getVatFluidBoundingBox(world, pos);
    }

    @Nullable
    public static AxisAlignedBB getVatFluidBoundingBox(World world, BlockPos masterPos) {
        TileEntity tileEntity = world.getTileEntity(masterPos);
        if (!(tileEntity instanceof TileEntityVat)) {
            return null;
        }
        TileEntityVat vat = (TileEntityVat) tileEntity;
        FluidStack fluid = vat.getTank().getFluid();
        if (fluid == null || fluid.amount <= 0) {
            return null;
        }

        float fillFraction = Math.min(1.0F, (float) fluid.amount / TileEntityVat.CAPACITY);
        float topY = BASIN_FLOOR_Y + fillFraction * (BASIN_RIM_Y - BASIN_FLOOR_Y);

        return rotateLocalBox(masterPos, vat.getPrimaryDirection(), BASIN_MIN_XZ, BASIN_FLOOR_Y, BASIN_MIN_XZ, BASIN_MAX_XZ, topY, BASIN_MAX_XZ);
    }

    @Override
    public int getFluidTintColor(World world, BlockPos pos) {
        return getVatFluidTintColor(world, pos);
    }

    public static int getVatFluidTintColor(World world, BlockPos masterPos) {
        TileEntity tileEntity = world.getTileEntity(masterPos);
        if (tileEntity instanceof TileEntityVat) {
            FluidStack fluid = ((TileEntityVat) tileEntity).getTank().getFluid();
            if (fluid != null) {
                return FluidHelper.getDisplayColor(fluid);
            }
        }
        return 0xFFFFFFFF;
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
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityVat();
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(ItemInit.VAT, 1, getMetaFromState(state));
    }

    @Nullable
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ItemInit.VAT;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntityVat)) {
            return false;
        }
        TileEntityVat vat = (TileEntityVat) tileEntity;

        ItemStack held = player.getHeldItem(hand);
        Fluid heldBucketFluid = FluidHelper.getBucketFluid(held.getItem());
        if (heldBucketFluid != null) {
            FluidStack fluidStack = new FluidStack(heldBucketFluid, 1000);
            if (vat.getTank().fill(fluidStack, false) >= fluidStack.amount) {
                if (!world.isRemote) {
                    vat.getTank().fill(fluidStack, true);
                    if (!player.capabilities.isCreativeMode) {
                        player.setHeldItem(hand, new ItemStack(Items.BUCKET));
                    }
                    world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.notifyBlockUpdate(pos, state, state, 3);
                }
                return true;
            }
        } else if (held.getItem() == Items.BUCKET) {
            FluidStack tankFluid = vat.getTank().getFluid();
            Item filledBucket = tankFluid != null ? FluidHelper.getFilledBucket(tankFluid.getFluid()) : null;
            if (filledBucket != null && tankFluid.amount >= 1000) {
                if (!world.isRemote) {
                    vat.getTank().drain(1000, true);
                    if (!player.capabilities.isCreativeMode) {
                        player.setHeldItem(hand, new ItemStack(filledBucket));
                    }
                    world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.notifyBlockUpdate(pos, state, state, 3);
                }
                return true;
            }
        }

        ItemStack solidContents = vat.getSolidContents();
        if (!solidContents.isEmpty()) {
            if (!world.isRemote) {
                dropOutputItem(world, pos, solidContents.copy());
                vat.collectSolidContents();
            }
            return true;
        }

        if (!world.isRemote) {
            player.openGui(PsychedelicraftResparked.instance, GuiHandler.VAT_ID, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityVat) {
            TileEntityVat vat = (TileEntityVat) tileEntity;
            InventoryHelper.dropInventoryItems(world, pos, vat);

            ItemStack solidContents = vat.getSolidContents();
            if (!solidContents.isEmpty()) {
                dropOutputItem(world, pos, solidContents.copy());
            }

            FluidStack fluid = vat.getTank().getFluid();
            if (fluid != null && fluid.amount > 0) {
                // Fluid itself isn't a droppable item - just lost on break.
                // TODO - Look into dropping with fluid still in, making barrel could add the plumbing
            }

            EnumFacing primary = vat.getPrimaryDirection();
            EnumFacing secondary = vat.getSecondaryDirection();
            if (primary != null && secondary != null) {
                for (BlockPos companionPos : new BlockPos[] {
                        pos.offset(primary),
                        pos.offset(secondary),
                        pos.offset(primary).offset(secondary)
                }) {
                    if (world.getBlockState(companionPos).getBlock() == BlockInit.VAT_COMPANION) {
                        world.setBlockToAir(companionPos);
                    }
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    private static void dropOutputItem(World world, BlockPos pos, ItemStack stack) {
        EntityItem entityItem = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entityItem.setDefaultPickupDelay();
        entityItem.addTag(TileEntityVat.OUTPUT_ITEM_TAG);
        world.spawnEntity(entityItem);
    }
}