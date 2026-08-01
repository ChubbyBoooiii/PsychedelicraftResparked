package com.chubbyboi.psychedelicraftresparked.util;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityBarrel;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDistillery;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityDryingTable;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityFlask;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityMashTub;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityMashTubCompanion;
import com.chubbyboi.psychedelicraftresparked.tileentities.TileEntityPeyote;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileEntityHandler {

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityDryingTable.class, new ResourceLocation(Tags.MOD_ID, "drying_table"));
        GameRegistry.registerTileEntity(TileEntityPeyote.class, new ResourceLocation(Tags.MOD_ID, "peyote"));
        GameRegistry.registerTileEntity(TileEntityMashTub.class, new ResourceLocation(Tags.MOD_ID, "mash_tub"));
        GameRegistry.registerTileEntity(TileEntityMashTubCompanion.class, new ResourceLocation(Tags.MOD_ID, "mash_tub_companion"));
        GameRegistry.registerTileEntity(TileEntityBarrel.class, new ResourceLocation(Tags.MOD_ID, "barrel"));
        GameRegistry.registerTileEntity(TileEntityDistillery.class, new ResourceLocation(Tags.MOD_ID, "distillery"));
        GameRegistry.registerTileEntity(TileEntityFlask.class, new ResourceLocation(Tags.MOD_ID, "flask"));
    }
}