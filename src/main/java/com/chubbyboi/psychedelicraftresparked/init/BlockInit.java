package com.chubbyboi.psychedelicraftresparked.init;

import com.chubbyboi.psychedelicraftresparked.block.BlockTallCrop;
import com.chubbyboi.psychedelicraftresparked.block.PsychBlockDryingTable;
import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockInit {
    public static final List<Block> BLOCKS = new ArrayList<>();

    // Functional Blocks
    public static final Block DRYING_TABLE = new PsychBlockDryingTable("drying_table");

    // Crops - only pass the crop item, seed and crop drops will be set later after items initialise
    public static final BlockTallCrop CANNABIS_PLANT = new BlockTallCrop("cannabis_plant", 15, 3);
    public static final BlockTallCrop TOBACCO_PLANT = new BlockTallCrop("tobacco_plant", 15, 3);

    // Call after init so the seeds and crops exist
    public static void setCropDropsAndSeeds() {
        // Set the seed item for the cannabis plant
        CANNABIS_PLANT.setSeedItem(ItemInit.CANNABIS_SEEDS);
        TOBACCO_PLANT.setSeedItem(ItemInit.TOBACCO_SEEDS);

        // Add crop drops: item, min amount, max amount
        CANNABIS_PLANT.addCropDrop(ItemInit.CANNABIS_BUD, 0, 2);
        CANNABIS_PLANT.addCropDrop(ItemInit.CANNABIS_LEAF, 1, 3);
        TOBACCO_PLANT.addCropDrop(ItemInit.TOBACCO_LEAF, 1, 8);
    }
}