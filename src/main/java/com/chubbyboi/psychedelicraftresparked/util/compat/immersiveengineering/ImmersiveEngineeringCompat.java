package com.chubbyboi.psychedelicraftresparked.util.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import com.chubbyboi.psychedelicraftresparked.block.BlockTallCrop;
import com.chubbyboi.psychedelicraftresparked.init.BlockInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ImmersiveEngineeringCompat {

    private static final ItemStack SOIL = new ItemStack(Blocks.DIRT);
    private static final BitByBitPlantHandler HANDLER = new BitByBitPlantHandler();

    public static void init() {
        register(ItemInit.CANNABIS_SEEDS, BlockInit.CANNABIS_PLANT,
            new ItemStack[]{new ItemStack(ItemInit.CANNABIS_BUD, 1), new ItemStack(ItemInit.CANNABIS_LEAF, 2)});
        register(ItemInit.TOBACCO_SEEDS, BlockInit.TOBACCO_PLANT,
            new ItemStack[]{new ItemStack(ItemInit.TOBACCO_LEAF, 2)});
        register(ItemInit.COCA_SEEDS, BlockInit.COCA_PLANT,
            new ItemStack[]{new ItemStack(ItemInit.COCA_LEAF, 2)});
        register(ItemInit.COFFEA_CHERRIES, BlockInit.COFFEA_PLANT,
            new ItemStack[]{new ItemStack(ItemInit.COFFEA_CHERRIES, 2)});
        register(ItemInit.HOP_SEEDS, BlockInit.HOPS_PLANT,
            new ItemStack[]{new ItemStack(ItemInit.HOP_CONES, 2)});

        BelljarHandler.registerHandler(HANDLER);
    }

    private static void register(Item seed, BlockTallCrop crop, ItemStack[] output) {
        HANDLER.trackCrop(seed, crop);
        // The render state passed here is unused (getRenderedPlant is overridden below to compute it
        // fresh from growth each frame) - just needs to be a valid non-null placeholder.
        HANDLER.register(new ItemStack(seed), output, SOIL, crop.getDefaultState());
    }

    private static final class BitByBitPlantHandler extends BelljarHandler.DefaultPlantHandler {
        private final HashSet<ComparableItemStack> validSeeds = new HashSet<>();
        private final Map<Item, BlockTallCrop> crops = new HashMap<>();

        void trackCrop(Item seedItem, BlockTallCrop crop) {
            crops.put(seedItem, crop);
        }

        @Override
        protected HashSet<ComparableItemStack> getSeedSet() {
            return validSeeds;
        }

        @Override
        public IBlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile) {
            BlockTallCrop crop = crops.get(seed.getItem());
            if (crop == null) {
                return null;
            }

            int maxAge = crop.getMaxAge();

            int visibleHeight = Math.min(crop.getMaxHeight(), 2);

            float levelProgress = growth * visibleHeight;
            int activeLevel = Math.min(visibleHeight - 1, (int) levelProgress);
            int activeAge = Math.min(maxAge, Math.round(maxAge * (levelProgress - activeLevel)));

            IBlockState matureState = crop.getDefaultState().withProperty(crop.getAgeProperty(), maxAge);

            IBlockState[] states = new IBlockState[activeLevel + 1];
            for (int i = 0; i < activeLevel; i++) {
                states[i] = matureState;
            }
            states[activeLevel] = crop.getDefaultState()
                .withProperty(crop.getAgeProperty(), activeAge)
                .withProperty(BlockTallCrop.TOP, true);

            return states;
        }
    }
}