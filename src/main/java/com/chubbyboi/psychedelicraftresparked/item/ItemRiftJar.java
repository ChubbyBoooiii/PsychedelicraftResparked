package com.chubbyboi.psychedelicraftresparked.item;

import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRiftJar extends ItemBlock {

    public ItemRiftJar(Block block) {
        super(block);
        setTranslationKey(block.getTranslationKey());
        setRegistryName(block.getRegistryName());
        setMaxStackSize(16);
        ItemInit.ITEMS.add(this);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;
        items.add(createFilledRiftJar(0.0f, this));
        items.add(createFilledRiftJar(0.25f, this));
        items.add(createFilledRiftJar(0.55f, this));
        items.add(createFilledRiftJar(0.75f, this));
        items.add(createFilledRiftJar(0.9f, this));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        float fraction = getRiftFraction(stack);
        if (fraction > 0.0f) {
            tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("psychedelicraftresparked.tooltip.rift_jar." + getFractionName(fraction)));
        }
    }

    public static float getRiftFraction(ItemStack itemStack) {
        return itemStack.hasTagCompound() ? itemStack.getTagCompound().getFloat("riftFraction") : 0.0f;
    }

    private static String getFractionName(float fraction) {
        if (fraction <= 0.0f) {
            return "empty";
        } else if (fraction < 0.4f) {
            return "slightly_filled";
        } else if (fraction < 0.6f) {
            return "half_filled";
        } else if (fraction < 0.8f) {
            return "full";
        } else {
            return "overflowing";
        }
    }

    public static ItemStack createFilledRiftJar(float riftFraction, net.minecraft.item.Item item) {
        ItemStack stack = new ItemStack(item);

        if (riftFraction > 0.0f) {
            stack.setTagInfo("riftFraction", new NBTTagFloat(riftFraction));
        }

        return stack;
    }
}