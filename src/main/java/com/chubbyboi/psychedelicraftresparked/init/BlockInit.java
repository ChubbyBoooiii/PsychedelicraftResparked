package com.chubbyboi.psychedelicraftresparked.init;

import com.chubbyboi.psychedelicraftresparked.block.BlockBarrel;
import com.chubbyboi.psychedelicraftresparked.block.BlockDistillery;
import com.chubbyboi.psychedelicraftresparked.block.BlockFlask;
import com.chubbyboi.psychedelicraftresparked.block.BlockMashTub;
import com.chubbyboi.psychedelicraftresparked.block.BlockMashTubCompanion;
import com.chubbyboi.psychedelicraftresparked.block.BlockPeyote;
import com.chubbyboi.psychedelicraftresparked.block.BlockTallCrop;
import com.chubbyboi.psychedelicraftresparked.block.PsychBlockDryingTable;
import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockInit {
    public static final List<Block> BLOCKS = new ArrayList<>();

    // Functional Blocks
    public static final Block DRYING_TABLE = new PsychBlockDryingTable("drying_table");
    public static final BlockMashTub MASH_TUB = new BlockMashTub("mash_tub");
    public static final BlockMashTubCompanion MASH_TUB_COMPANION = new BlockMashTubCompanion("mash_tub_companion");
    public static final BlockBarrel BARREL = new BlockBarrel("barrel");
    public static final BlockDistillery DISTILLERY = new BlockDistillery("distillery");
    public static final BlockFlask FLASK = new BlockFlask("flask");

    // Crops - only pass the crop item, seed and crop drops will be set later after items initialise
    public static final BlockTallCrop CANNABIS_PLANT = new BlockTallCrop("cannabis_plant", 15, 3);
    public static final BlockTallCrop TOBACCO_PLANT = new BlockTallCrop("tobacco_plant", 15, 3);
    public static final BlockTallCrop COCA_PLANT = new BlockTallCrop("coca_plant", 15, 3);
    public static final BlockTallCrop COFFEA_PLANT = new BlockTallCrop("coffea_plant", 15, 2);

    public static final BlockPeyote PEYOTE_PLANT = new BlockPeyote("peyote");

    // Call after init so the seeds and crops exist
    public static void setCropDropsAndSeeds() {
        // Set the seed item for the cannabis plant
        CANNABIS_PLANT.setSeedItem(ItemInit.CANNABIS_SEEDS);
        TOBACCO_PLANT.setSeedItem(ItemInit.TOBACCO_SEEDS);
        COCA_PLANT.setSeedItem(ItemInit.COCA_SEEDS);
        COFFEA_PLANT.setSeedItem(ItemInit.COFFEA_CHERRIES);

        // Add crop drops: item, min amount, max amount
        CANNABIS_PLANT.addCropDrop(ItemInit.CANNABIS_BUD, 0, 2);
        CANNABIS_PLANT.addCropDrop(ItemInit.CANNABIS_LEAF, 2, 5);
        TOBACCO_PLANT.addCropDrop(ItemInit.TOBACCO_LEAF, 3, 8);
        COCA_PLANT.addCropDrop(ItemInit.COCA_LEAF, 2, 6);
        COFFEA_PLANT.addCropDrop(ItemInit.COFFEA_CHERRIES, 2, 6);
    }
}