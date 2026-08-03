package com.chubbyboi.psychedelicraftresparked.util;

import com.chubbyboi.psychedelicraftresparked.gui.BarrelContainer;
import com.chubbyboi.psychedelicraftresparked.gui.BarrelGui;
import com.chubbyboi.psychedelicraftresparked.gui.DistilleryContainer;
import com.chubbyboi.psychedelicraftresparked.gui.DistilleryGui;
import com.chubbyboi.psychedelicraftresparked.gui.DryingTableContainer;
import com.chubbyboi.psychedelicraftresparked.gui.DryingTableGui;
import com.chubbyboi.psychedelicraftresparked.gui.FlaskContainer;
import com.chubbyboi.psychedelicraftresparked.gui.FlaskGui;
import com.chubbyboi.psychedelicraftresparked.gui.VatContainer;
import com.chubbyboi.psychedelicraftresparked.gui.VatGui;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBarrel;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDistillery;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDryingTable;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityFlask;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityVat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int DRYING_TABLE_ID = 0;
    public static final int VAT_ID = 1;
    public static final int BARREL_ID = 2;
    public static final int DISTILLERY_ID = 3;
    public static final int FLASK_ID = 4;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == DRYING_TABLE_ID) return new DryingTableContainer(player.inventory, (TileEntityDryingTable)world.getTileEntity(new BlockPos(x, y, z)));
        if (ID == VAT_ID) return new VatContainer(player.inventory, (TileEntityVat)world.getTileEntity(new BlockPos(x, y, z)));
        if (ID == BARREL_ID) return new BarrelContainer(player.inventory, (TileEntityBarrel)world.getTileEntity(new BlockPos(x, y, z)));
        if (ID == DISTILLERY_ID) return new DistilleryContainer(player.inventory, (TileEntityDistillery)world.getTileEntity(new BlockPos(x, y, z)));
        if (ID == FLASK_ID) return new FlaskContainer(player.inventory, (TileEntityFlask)world.getTileEntity(new BlockPos(x, y, z)));
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == DRYING_TABLE_ID) return new DryingTableGui(player.inventory, (TileEntityDryingTable)world.getTileEntity(new BlockPos(x, y, z)));
        if (ID == VAT_ID) return new VatGui(player.inventory, (TileEntityVat)world.getTileEntity(new BlockPos(x, y, z)));
        if (ID == BARREL_ID) return new BarrelGui(player.inventory, (TileEntityBarrel)world.getTileEntity(new BlockPos(x, y, z)));
        if (ID == DISTILLERY_ID) return new DistilleryGui(player.inventory, (TileEntityDistillery)world.getTileEntity(new BlockPos(x, y, z)));
        if (ID == FLASK_ID) return new FlaskGui(player.inventory, (TileEntityFlask)world.getTileEntity(new BlockPos(x, y, z)));
        return null;
    }
}