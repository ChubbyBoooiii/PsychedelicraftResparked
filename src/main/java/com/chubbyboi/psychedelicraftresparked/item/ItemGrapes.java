package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.block.BlockLattice;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemGrapes extends PsychFoodItem {
    public ItemGrapes(String name, int amount, float saturationMultiplier) {
        super(name, amount, saturationMultiplier);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockLattice) {
            BlockLattice lattice = (BlockLattice) state.getBlock();
            if (lattice.tryPlant(world, pos, state)) {
                if (!world.isRemote && !player.capabilities.isCreativeMode) {
                    player.getHeldItem(hand).shrink(1);
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }
}
