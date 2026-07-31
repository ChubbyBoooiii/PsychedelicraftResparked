package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityMashTub;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityMashTubCompanion;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemMashTub extends Item {

    public ItemMashTub(String name) {
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);
        setMaxStackSize(1);
        ItemInit.ITEMS.add(this);
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
            world.setBlockState(masterPos, BlockInit.MASH_TUB.getDefaultState());
            world.setBlockState(primaryPos, BlockInit.MASH_TUB_COMPANION.getDefaultState());
            world.setBlockState(secondaryPos, BlockInit.MASH_TUB_COMPANION.getDefaultState());
            world.setBlockState(diagonalPos, BlockInit.MASH_TUB_COMPANION.getDefaultState());

            TileEntity tileEntity = world.getTileEntity(masterPos);
            if (tileEntity instanceof TileEntityMashTub) {
                ((TileEntityMashTub) tileEntity).setPrimaryDirection(primary);
                world.notifyBlockUpdate(masterPos, world.getBlockState(masterPos), world.getBlockState(masterPos), 3);
            }

            for (BlockPos companionPos : new BlockPos[] {primaryPos, secondaryPos, diagonalPos}) {
                TileEntity companionTe = world.getTileEntity(companionPos);
                if (companionTe instanceof TileEntityMashTubCompanion) {
                    ((TileEntityMashTubCompanion) companionTe).setMasterPos(masterPos);
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