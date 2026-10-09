package com.chubbyboi.psychedelicraftresparked.recipes;

import com.chubbyboi.psychedelicraftresparked.block.ContainerShape;
import com.chubbyboi.psychedelicraftresparked.block.PlacedContainerType;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.chubbyboi.psychedelicraftresparked.item.ItemBottle;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

public final class BottleWorkbenchRecipes {

    // Forge's ore dictionary colour names
    private static final String[] ORE_COLOURS = {
        "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan", "LightGray",
        "Gray", "Pink", "Lime", "Yellow", "LightBlue", "Magenta", "Orange", "White"
    };

    private BottleWorkbenchRecipes() {
    }

    public static boolean isGlass(ItemStack stack) {
        return hasOre(stack, "blockGlass");
    }

    public static boolean isDye(ItemStack stack) {
        return getDyeColour(stack) != null;
    }

    public static int getGlassMeta(ItemStack stack) {
        for (EnumDyeColor colour : EnumDyeColor.values()) {
            String name = ORE_COLOURS[colour.getDyeDamage()];
            if (hasOre(stack, "blockGlass" + name)) {
                return colour.getMetadata();
            }
        }
        return ItemBottle.CLEAR_META;
    }

    public static EnumDyeColor getDyeColour(ItemStack stack) {
        for (EnumDyeColor colour : EnumDyeColor.values()) {
            if (hasOre(stack, "dye" + ORE_COLOURS[colour.getDyeDamage()])) {
                return colour;
            }
        }
        return null;
    }

    public static ItemStack getResult(ItemStack glass, ItemStack dye, ContainerShape shape) {
        if (glass.isEmpty() || !isGlass(glass)) {
            return ItemStack.EMPTY;
        }
        EnumDyeColor dyeColour = dye.isEmpty() ? null : getDyeColour(dye);
        int meta = dyeColour != null ? dyeColour.getMetadata() : getGlassMeta(glass);
        return PlacedContainerType.withShape(new ItemStack(ItemInit.BOTTLE, 1, meta), shape.name);
    }

    public static List<ContainerShape> getShapes() {
        return PlacedContainerType.BOTTLE.getShapes();
    }

    private static boolean hasOre(ItemStack stack, String ore) {
        if (stack.isEmpty() || !OreDictionary.doesOreNameExist(ore)) {
            return false;
        }
        int id = OreDictionary.getOreID(ore);
        for (int stackId : OreDictionary.getOreIDs(stack)) {
            if (stackId == id) {
                return true;
            }
        }
        return false;
    }
}