package com.chubbyboi.psychedelicraftresparked.recipes;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.FluidInit;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CraftingRecipes {

    public static void registerCraftingRecipes() {
        // ==================== BLOCKS ====================
        // Drying Table
        GameRegistry.addShapedRecipe(new ResourceLocation(Tags.MOD_ID, "drying_table"), null, new ItemStack(ItemInit.DRYING_TABLE_ITEM, 1), "WWW", "WRW", 'R', "dustRedstone", 'W', "plankWood");



        // ==================== ITEMS ====================
        // Drug Delivery Items
        GameRegistry.addShapedRecipe(new ResourceLocation(Tags.MOD_ID, "pipe"), null, new ItemStack(ItemInit.PIPE, 1), "  I", " S ", "WS ", 'I', "ingotIron", 'S', "stickWood", 'W', "plankWood");
        GameRegistry.addShapedRecipe(new ResourceLocation(Tags.MOD_ID, "bong"), null, new ItemStack(ItemInit.BONG, 1), " P ", "G G", "GGG", 'G', "blockGlassColorless", 'P', "paneGlassColorless");
        GameRegistry.addShapedRecipe(new ResourceLocation(Tags.MOD_ID, "syringe"), null, new ItemStack(ItemInit.SYRINGE, 1), "I", "G", 'G', "blockGlassColorless", 'I', "ingotIron");


        // Cannabis
        GameRegistry.addShapedRecipe(new ResourceLocation(Tags.MOD_ID, "joint"), null, new ItemStack(ItemInit.JOINT, 1), "P", "C", "P", 'C', ItemInit.DRIED_CANNABIS_BUDS, 'P', Items.PAPER);
        GameRegistry.addShapedRecipe(new ResourceLocation(Tags.MOD_ID, "blunt"), null, new ItemStack(ItemInit.BLUNT, 1), "TTT", "CCC", "PPP", 'C', ItemInit.DRIED_CANNABIS_BUDS, 'P', Items.PAPER, 'T', ItemInit.DRIED_TOBACCO);
        GameRegistry.addShapedRecipe(new ResourceLocation(Tags.MOD_ID, "hash_muffin"), null, new ItemStack(ItemInit.HASH_MUFFIN, 1), "LLL", "WCW", "LLL", 'C', new ItemStack(Items.DYE, 1, 3), 'L', ItemInit.DRIED_CANNABIS_LEAVES, 'W', "cropWheat");

        // Cocaine
        GameRegistry.addShapelessRecipe(new ResourceLocation(Tags.MOD_ID, "cocaine_powder"), null, new ItemStack(ItemInit.COCAINE_POWDER, 1), Ingredient.fromItem(ItemInit.DRIED_COCA_LEAVES));

        // Tobacco
        GameRegistry.addShapedRecipe(new ResourceLocation(Tags.MOD_ID, "cigarette"), null, new ItemStack(ItemInit.CIGARETTE, 4), "P", "T", "P", 'P', Items.PAPER, 'T', ItemInit.DRIED_TOBACCO);
        GameRegistry.addShapedRecipe(new ResourceLocation(Tags.MOD_ID, "cigar"), null, new ItemStack(ItemInit.CIGAR, 1), "TTT", "TTT", "PPP", 'P', Items.PAPER, 'T', ItemInit.DRIED_TOBACCO);

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

        // Coffee
        GameRegistry.addSmelting(ItemInit.COFFEA_CHERRIES, new ItemStack(ItemInit.COFFEE_BEANS), 0.2F);



        // ==================== FLUID FILLING ====================
        ForgeRegistries.RECIPES.register(new RecipeFillContainer(new FluidStack(FluidInit.COCAINE_FLUID, 10), Ingredient.fromItem(ItemInit.COCAINE_POWDER), Ingredient.fromItem(Items.WATER_BUCKET)).setRegistryName(Tags.MOD_ID, "fill_syringe_cocaine"));
        ForgeRegistries.RECIPES.register(new RecipeFillContainer(new FluidStack(FluidInit.COFFEE, 500), Ingredient.fromItem(ItemInit.COFFEE_BEANS), Ingredient.fromItem(ItemInit.COFFEE_BEANS), Ingredient.fromItem(Items.WATER_BUCKET)).setRegistryName(Tags.MOD_ID, "fill_mug_coffee"));
    }
}