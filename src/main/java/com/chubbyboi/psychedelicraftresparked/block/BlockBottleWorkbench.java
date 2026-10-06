package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.util.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBottleWorkbench extends Block {

    public BlockBottleWorkbench(String name) {
        super(Material.WOOD);

        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);

        this.setHardness(2.5F);
        this.setSoundType(SoundType.WOOD);

        BlockInit.BLOCKS.add(this);
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
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(PsychedelicraftResparked.instance, GuiHandler.BOTTLE_WORKBENCH_ID, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }
}