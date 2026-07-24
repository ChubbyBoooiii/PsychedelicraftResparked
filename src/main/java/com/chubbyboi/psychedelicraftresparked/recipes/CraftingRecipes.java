package com.chubbyboi.psychedelicraftresparked.recipes;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CraftingRecipes {

    public static void registerCraftingRecipes() {
        // Drying Table
        GameRegistry.addShapedRecipe(
            new ResourceLocation(Tags.MOD_ID, "drying_table"),
            null,
            new ItemStack(ItemInit.DRYING_TABLE_ITEM, 1),
            "WWW",
            "WRW",
            'R', "dustRedstone",
            'W', "plankWood"
        );

        // Joint
        GameRegistry.addShapedRecipe(
            new ResourceLocation(Tags.MOD_ID, "joint"),
            null,
            new ItemStack(ItemInit.JOINT, 1),
            "P",
            "C",
            "P",
            'C', ItemInit.DRIED_CANNABIS_BUDS,
            'P', Items.PAPER
        );

        // Cigar
        GameRegistry.addShapedRecipe(
            new ResourceLocation(Tags.MOD_ID, "blunt"),
            null,
            new ItemStack(ItemInit.BLUNT, 1),
            "TTT",
            "CCC",
            "PPP",
            'C', ItemInit.DRIED_CANNABIS_BUDS,
            'P', Items.PAPER,
            'T', ItemInit.DRIED_TOBACCO
        );

        // Hash Muffin
        GameRegistry.addShapedRecipe(
            new ResourceLocation(Tags.MOD_ID, "hash_muffin"),
            null,
            new ItemStack(ItemInit.HASH_MUFFIN, 1),
            "LLL",
            "WCW",
            "LLL",
            'C', new ItemStack(Items.DYE, 1, 3), // Cocoa Beans (Brown Dye)
            'L', ItemInit.DRIED_CANNABIS_LEAVES,
            'W', "cropWheat"
        );

        // Cigarette
        GameRegistry.addShapedRecipe(
            new ResourceLocation(Tags.MOD_ID, "cigarette"),
            null,
            new ItemStack(ItemInit.CIGARETTE, 4),
            "P",
            "T",
            "P",
            'P', Items.PAPER,
            'T', ItemInit.DRIED_TOBACCO
        );

        // Cigar
        GameRegistry.addShapedRecipe(
            new ResourceLocation(Tags.MOD_ID, "cigar"),
            null,
            new ItemStack(ItemInit.CIGAR, 1),
            "TTT",
            "TTT",
            "PPP",
            'P', Items.PAPER,
            'T', ItemInit.DRIED_TOBACCO
        );

        // Pipe
        GameRegistry.addShapedRecipe(
            new ResourceLocation(Tags.MOD_ID, "pipe"),
            null,
            new ItemStack(ItemInit.PIPE, 1),
            "  I",
            " S ",
            "WS ",
            'I', "ingotIron",
            'S', "stickWood",
            'W', "plankWood"
        );

        // Bong
        GameRegistry.addShapedRecipe(
            new ResourceLocation(Tags.MOD_ID, "bong"),
            null,
            new ItemStack(ItemInit.BONG, 1),
            " P ",
            "G G",
            "GGG",
            'G', "blockGlassColorless",
            'P', "paneGlassColorless"
        );

        // Harmonium
        for (EnumDyeColor color : EnumDyeColor.values()) {
            GameRegistry.addShapelessRecipe(
                new ResourceLocation(Tags.MOD_ID, "harmonium_" + color.getName()),
                null,
                new ItemStack(ItemInit.HARMONIUM, 1, color.getMetadata()),
                Ingredient.fromStacks(new ItemStack(Items.DYE, 1, color.getDyeDamage())),
                Ingredient.fromItem(Items.GLOWSTONE_DUST),
                Ingredient.fromItem(ItemInit.DRIED_TOBACCO)
            );
        }
    }
}