package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityPlacedContainers;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockPlacedContainers extends Block implements ITileEntityProvider {

    private static final AxisAlignedBB FALLBACK_SHAPE = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.5, 0.75);

    public BlockPlacedContainers(String name) {
        super(Material.GLASS);
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setHardness(0.3F);
        setSoundType(SoundType.GLASS);
        BlockInit.BLOCKS.add(this);
    }

    public static final class Placement {
        public final PlacedContainerType type;
        public final ContainerShape shape;
        public final BlockPos target;
        public final boolean existing;
        public final float x;
        public final float z;
        public final int rotation;
        public final boolean fits;

        private Placement(PlacedContainerType type, ContainerShape shape, BlockPos target, boolean existing, float x, float z, int rotation, boolean fits) {
            this.type = type;
            this.shape = shape;
            this.target = target;
            this.existing = existing;
            this.x = x;
            this.z = z;
            this.rotation = rotation;
            this.fits = fits;
        }
    }

    @Nullable
    public static Placement findPlacement(World world, BlockPos clicked, EnumFacing facing, EntityPlayer player, ItemStack stack, float hitX, float hitZ) {
        PlacedContainerType type = PlacedContainerType.of(stack);
        if (type == null || facing != EnumFacing.UP) {
            return null;
        }

        BlockPos target;
        if (world.getBlockState(clicked).getBlock() == BlockInit.PLACED_CONTAINERS) {
            target = clicked;
        } else {
            if (!canSupport(world, clicked)) {
                return null;
            }
            target = clicked.up();
        }

        Block targetBlock = world.getBlockState(target).getBlock();
        boolean existing = targetBlock == BlockInit.PLACED_CONTAINERS;
        if (!existing && !targetBlock.isReplaceable(world, target)) {
            return null;
        }
        if (!player.canPlayerEdit(target, facing, stack)) {
            return null;
        }

        ContainerShape shape = type.getShape(stack);
        float x = TileEntityPlacedContainers.snapToBlock(hitX * 16.0F, shape);
        float z = TileEntityPlacedContainers.snapToBlock(hitZ * 16.0F, shape);
        int rotation = MathHelper.floor((180.0F + player.rotationYaw) * TileEntityPlacedContainers.ROTATION_STEPS / 360.0F + 0.5F)
            & (TileEntityPlacedContainers.ROTATION_STEPS - 1);

        boolean fits = true;
        if (existing) {
            TileEntity tileEntity = world.getTileEntity(target);
            fits = tileEntity instanceof TileEntityPlacedContainers && ((TileEntityPlacedContainers) tileEntity).canPlace(shape, x, z);
        }
        return new Placement(type, shape, target, existing, x, z, rotation, fits);
    }

    public static EnumActionResult tryPlace(World world, BlockPos clicked, EnumFacing facing, EntityPlayer player, EnumHand hand, float hitX, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        Placement placement = findPlacement(world, clicked, facing, player, stack, hitX, hitZ);
        if (placement == null) {
            return EnumActionResult.PASS;
        }
        if (world.isRemote || !placement.fits) {
            return EnumActionResult.SUCCESS;
        }

        PlacedContainerType type = placement.type;
        BlockPos target = placement.target;
        if (!placement.existing) {
            world.setBlockState(target, BlockInit.PLACED_CONTAINERS.getDefaultState(), 3);
        }

        TileEntity tileEntity = world.getTileEntity(target);
        if (!(tileEntity instanceof TileEntityPlacedContainers)) {
            return EnumActionResult.SUCCESS;
        }

        ItemStack placed = stack.copy();
        placed.setCount(1);
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        ((TileEntityPlacedContainers) tileEntity).add(placed, type, placement.x, placement.z, placement.rotation);

        SoundType sound = type.soundType;
        world.playSound(null, target, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        return EnumActionResult.SUCCESS;
    }

    private static boolean canSupport(World world, BlockPos pos) {
        return world.getBlockState(pos).isSideSolid(world, pos, EnumFacing.UP);
    }

    @Nullable
    private static TileEntityPlacedContainers getContainers(IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity instanceof TileEntityPlacedContainers ? (TileEntityPlacedContainers) tileEntity : null;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityPlacedContainers();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND || !player.getHeldItemMainhand().isEmpty()) {
            return false;
        }

        TileEntityPlacedContainers containers = getContainers(world, pos);
        if (containers == null) {
            return false;
        }
        int index = containers.findEntry(hitX, hitY, hitZ);
        if (index < 0) {
            return false;
        }

        if (!world.isRemote) {
            if (player.isSneaking()) {
                containers.rotate(index);
            } else {
                player.setHeldItem(EnumHand.MAIN_HAND, containers.remove(index));
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                    ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                if (containers.isEmpty()) {
                    world.setBlockToAir(pos);
                }
            }
        }
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!canSupport(world, pos.down())) {
            world.destroyBlock(pos, false);
        }
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        TileEntityPlacedContainers containers = getContainers(world, pos);
        if (containers == null || containers.isEmpty()) {
            return SoundType.GLASS;
        }
        SoundType shared = containers.getEntries().get(0).type.soundType;
        for (TileEntityPlacedContainers.Entry entry : containers.getEntries()) {
            if (entry.type.soundType != shared) {
                return SoundType.GLASS;
            }
        }
        return shared;
    }

    // Creative breaks drop nothing
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (player.capabilities.isCreativeMode) {
            TileEntityPlacedContainers containers = getContainers(world, pos);
            if (containers != null) {
                containers.clearForRemoval();
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntityPlacedContainers containers = getContainers(world, pos);
        if (containers != null) {
            for (TileEntityPlacedContainers.Entry entry : containers.getEntries()) {
                spawnAsEntity(world, pos, entry.stack.copy());
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntityPlacedContainers containers = getContainers(world, pos);
        if (containers != null && target.subHit >= 0 && target.subHit < containers.getEntries().size()) {
            ItemStack stack = containers.getEntries().get(target.subHit).stack.copy();
            stack.setCount(1);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileEntityPlacedContainers containers = getContainers(source, pos);
        if (containers == null || containers.isEmpty()) {
            return FALLBACK_SHAPE;
        }
        AxisAlignedBB union = null;
        for (TileEntityPlacedContainers.Entry entry : containers.getEntries()) {
            union = union == null ? entry.getBox() : union.union(entry.getBox());
        }
        return union;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
        TileEntityPlacedContainers containers = getContainers(world, pos);
        if (containers == null) {
            return;
        }
        for (TileEntityPlacedContainers.Entry entry : containers.getEntries()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, entry.getBox());
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        TileEntityPlacedContainers containers = getContainers(world, pos);
        if (containers == null) {
            return null;
        }
        RayTraceResult nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        List<TileEntityPlacedContainers.Entry> entries = containers.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            RayTraceResult hit = rayTrace(pos, start, end, entries.get(i).getBox());
            if (hit != null) {
                double distance = hit.hitVec.squareDistanceTo(start);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = hit;
                    nearest.subHit = i;
                }
            }
        }
        return nearest;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
        TileEntityPlacedContainers containers = getContainers(world, pos);
        if (hit != null && containers != null && pos.equals(hit.getBlockPos())
                && hit.subHit >= 0 && hit.subHit < containers.getEntries().size()) {
            return containers.getEntries().get(hit.subHit).getBox().offset(pos);
        }
        return super.getSelectedBoundingBox(state, world, pos);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
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
}