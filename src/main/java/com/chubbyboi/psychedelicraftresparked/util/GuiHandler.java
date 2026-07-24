package com.chubbyboi.psychedelicraftresparked.util;

import com.chubbyboi.psychedelicraftresparked.gui.DryingTableContainer;
import com.chubbyboi.psychedelicraftresparked.gui.DryingTableGui;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDryingTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int DRYING_TABLE_ID = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == DRYING_TABLE_ID) return new DryingTableContainer(player.inventory, (TileEntityDryingTable)world.getTileEntity(new BlockPos(x, y, z)));
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == DRYING_TABLE_ID) return new DryingTableGui(player.inventory, (TileEntityDryingTable)world.getTileEntity(new BlockPos(x, y, z)));
        return null;
    }
}
