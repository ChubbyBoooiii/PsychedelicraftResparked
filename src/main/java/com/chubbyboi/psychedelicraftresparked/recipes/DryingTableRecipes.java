package com.chubbyboi.psychedelicraftresparked.recipes;

import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import com.google.common.collect.Maps;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.Map.Entry;

public class DryingTableRecipes {

    private static final DryingTableRecipes INSTANCE = new DryingTableRecipes();
    private final Map<ItemStack, ItemStack> dryingList = Maps.newHashMap();
    private final Map<ItemStack, Float> experienceList = Maps.newHashMap();

    public static DryingTableRecipes getInstance() {
        return INSTANCE;
    }

    private DryingTableRecipes() {
        addDryingTableRecipes(new ItemStack(ItemInit.CANNABIS_LEAF), new ItemStack(ItemInit.DRIED_CANNABIS_LEAVES, 3), 5.0F);
        addDryingTableRecipes(new ItemStack(ItemInit.CANNABIS_BUD), new ItemStack(ItemInit.DRIED_CANNABIS_BUDS, 3), 5.0F);
        addDryingTableRecipes(new ItemStack(ItemInit.TOBACCO_LEAF), new ItemStack(ItemInit.DRIED_TOBACCO, 3), 5.0F);
        addDryingTableRecipes(new ItemStack(Blocks.BROWN_MUSHROOM), new ItemStack(ItemInit.BROWN_SHROOMS, 3), 5.0F);
        addDryingTableRecipes(new ItemStack(Blocks.RED_MUSHROOM), new ItemStack(ItemInit.RED_SHROOMS, 3), 5.0F);
        addDryingTableRecipes(new ItemStack(ItemInit.COCA_LEAF), new ItemStack(ItemInit.DRIED_COCA_LEAVES, 3), 5.0F);
    }

    public void addDryingTableRecipes(ItemStack inputs, ItemStack result, float experience) {
        if (getDryingResult(inputs) != ItemStack.EMPTY) return;
        this.dryingList.put(inputs, result);
        this.experienceList.put(result, experience);
    }

    public ItemStack getDryingResult(ItemStack inputs) {
        for (Entry<ItemStack, ItemStack> entry : this.dryingList.entrySet()) {
            if (this.compareItemStacks(inputs, entry.getKey())) {
                return entry.getValue();
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean compareItemStacks(ItemStack inputs, ItemStack recipeInputs) {
        return recipeInputs.getItem() == inputs.getItem() && (recipeInputs.getMetadata() == 32767 || recipeInputs.getMetadata() == inputs.getMetadata());
    }

    public float getDryingExperience(ItemStack result) {
        for (Entry<ItemStack, Float> entry : this.experienceList.entrySet()) {
            if (this.compareItemStacks(result, entry.getKey())) {
                return entry.getValue();
            }
        }
        return 0.0F;
    }
}
