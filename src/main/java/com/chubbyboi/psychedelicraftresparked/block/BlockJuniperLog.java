package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.PsychedelicraftResparked;
import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class BlockJuniperLog extends BlockLog {
    public BlockJuniperLog(String name) {
        setTranslationKey(name);
        setRegistryName(Tags.MOD_ID, name);
        setCreativeTab(PsychedelicraftResparked.PSYCHTAB);
        setDefaultState(this.blockState.getBaseState().withProperty(LOG_AXIS, BlockLog.EnumAxis.Y));
        BlockInit.BLOCKS.add(this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LOG_AXIS);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        BlockLog.EnumAxis axis;
        switch (meta & 12) {
            case 4:
                axis = BlockLog.EnumAxis.X;
                break;
            case 8:
                axis = BlockLog.EnumAxis.Z;
                break;
            case 12:
                axis = BlockLog.EnumAxis.NONE;
                break;
            default:
                axis = BlockLog.EnumAxis.Y;
        }
        return getDefaultState().withProperty(LOG_AXIS, axis);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        switch (state.getValue(LOG_AXIS)) {
            case X:
                return 4;
            case Z:
                return 8;
            case NONE:
                return 12;
            default:
                return 0;
        }
    }
}