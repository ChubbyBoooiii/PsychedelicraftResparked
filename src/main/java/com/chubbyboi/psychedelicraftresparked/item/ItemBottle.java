package com.chubbyboi.psychedelicraftresparked.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemBottle extends ItemDrinkable {

    public ItemBottle(String name, int capacity, int consumptionVolume, int useDuration) {
        super(name, capacity, consumptionVolume, useDuration, ConsumptionType.DRINK);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) {
            return;
        }
        for (int meta = 0; meta < 16; meta++) {
            items.add(new ItemStack(this, 1, meta));
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey() + "." + EnumDyeColor.byMetadata(stack.getMetadata()).getTranslationKey();
    }
}