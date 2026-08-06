package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVatCompanion;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemVat extends Item {

    public ItemVat(String name) {
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
        ItemInit.ITEMS.add(this);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) {
            return;
        }
        for (BlockPlanks.EnumType type : BlockPlanks.EnumType.values()) {
            items.add(new ItemStack(this, 1, type.getMetadata()));
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey() + "." + BlockPlanks.EnumType.byMetadata(stack.getMetadata()).getName();
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        BlockPos masterPos = facing == EnumFacing.UP ? pos.up() : pos;

        EnumFacing primary = player.getHorizontalFacing();
        EnumFacing secondary = primary.rotateY();
        BlockPos primaryPos = masterPos.offset(primary);
        BlockPos secondaryPos = masterPos.offset(secondary);
        BlockPos diagonalPos = primaryPos.offset(secondary);

        for (BlockPos cellPos : new BlockPos[] {masterPos, primaryPos, secondaryPos, diagonalPos}) {
            if (!world.isAirBlock(cellPos) || !canSustain(world, cellPos)) {
                return EnumActionResult.FAIL;
            }
        }

        if (!world.isRemote) {
            int woodMeta = player.getHeldItem(hand).getMetadata();
            world.setBlockState(masterPos, BlockInit.VAT.getStateFromMeta(woodMeta));
            world.setBlockState(primaryPos, BlockInit.VAT_COMPANION.getDefaultState());
            world.setBlockState(secondaryPos, BlockInit.VAT_COMPANION.getDefaultState());
            world.setBlockState(diagonalPos, BlockInit.VAT_COMPANION.getDefaultState());

            IBlockState placedState = world.getBlockState(masterPos);
            SoundType soundType = placedState.getBlock().getSoundType(placedState, world, masterPos, player);
            world.playSound(null, masterPos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

            TileEntity tileEntity = world.getTileEntity(masterPos);
            if (tileEntity instanceof TileEntityVat) {
                ((TileEntityVat) tileEntity).setPrimaryDirection(primary);
                world.notifyBlockUpdate(masterPos, world.getBlockState(masterPos), world.getBlockState(masterPos), 3);
            }

            for (BlockPos companionPos : new BlockPos[] {primaryPos, secondaryPos, diagonalPos}) {
                TileEntity companionTe = world.getTileEntity(companionPos);
                if (companionTe instanceof TileEntityVatCompanion) {
                    ((TileEntityVatCompanion) companionTe).setMasterPos(masterPos);
                    world.notifyBlockUpdate(companionPos, world.getBlockState(companionPos), world.getBlockState(companionPos), 3);
                }
            }

            if (!player.capabilities.isCreativeMode) {
                player.getHeldItem(hand).shrink(1);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    private boolean canSustain(World world, BlockPos pos) {
        BlockPos below = pos.down();
        return world.getBlockState(below).isSideSolid(world, below, EnumFacing.UP);
    }
}