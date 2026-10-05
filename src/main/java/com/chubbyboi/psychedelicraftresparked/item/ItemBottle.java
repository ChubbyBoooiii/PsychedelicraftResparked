package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.block.ContainerShape;
import com.chubbyboi.psychedelicraftresparked.block.PlacedContainerType;
import net.minecraft.block.material.MapColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class ItemBottle extends ItemDrinkable {

    public static final int CLEAR_META = 16;

    public ItemBottle(String name, int capacity, int consumptionVolume, int useDuration) {
        super(name, capacity, consumptionVolume, useDuration, ConsumptionType.DRINK);
        setHasSubtypes(true);
        setMaxDamage(0);

        // Index into the bottle shapes, so the item model can override display transforms per shape
        this.addPropertyOverride(new ResourceLocation("shape"), (stack, worldIn, entityIn) ->
            PlacedContainerType.BOTTLE.getShapes().indexOf(PlacedContainerType.BOTTLE.getShape(stack)));
    }

    public static boolean isClear(int meta) {
        return meta == CLEAR_META;
    }

    public static int getGlassColor(int meta) {
        return isClear(meta) ? 0xFFFFFF : MapColor.getBlockColor(EnumDyeColor.byMetadata(meta)).colorValue;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) {
            return;
        }
        for (ContainerShape shape : PlacedContainerType.BOTTLE.getShapes()) {
            items.add(PlacedContainerType.withShape(new ItemStack(this, 1, CLEAR_META), shape.name));
            for (int meta = 0; meta < 16; meta++) {
                items.add(PlacedContainerType.withShape(new ItemStack(this, 1, meta), shape.name));
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if (isClear(stack.getMetadata())) {
            return super.getTranslationKey();
        }
        return super.getTranslationKey() + "." + EnumDyeColor.byMetadata(stack.getMetadata()).getTranslationKey();
    }
}