package com.chubbyboi.psychedelicraftresparked.block;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class BottleLabel {

    public static final String LABEL_TAG = "Label";
    private static final String SHAPE_TAG = "Shape";
    private static final String COLOR_TAG = "Color";

    public static final List<String> SHAPES = Collections.unmodifiableList(Arrays.asList("band", "sticker"));

    private BottleLabel() {
    }

    public static boolean hasLabel(ItemStack stack) {
        return getShape(stack) != null;
    }

    @Nullable
    public static String getShape(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(LABEL_TAG, Constants.NBT.TAG_COMPOUND)) {
            return null;
        }
        String shape = tag.getCompoundTag(LABEL_TAG).getString(SHAPE_TAG);
        return SHAPES.contains(shape) ? shape : null;
    }

    public static EnumDyeColor getColor(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        int meta = tag != null ? tag.getCompoundTag(LABEL_TAG).getInteger(COLOR_TAG) : 0;
        return EnumDyeColor.byMetadata(meta);
    }

    public static ItemStack withLabel(ItemStack stack, String shape, EnumDyeColor color) {
        NBTTagCompound label = new NBTTagCompound();
        label.setString(SHAPE_TAG, shape);
        label.setInteger(COLOR_TAG, color.getMetadata());
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setTag(LABEL_TAG, label);
        return stack;
    }

    public static ItemStack removeLabel(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            tag.removeTag(LABEL_TAG);
            // An empty tag would stop it stacking with plain bottles
            if (tag.isEmpty()) {
                stack.setTagCompound(null);
            }
        }
        return stack;
    }

    public static void copy(ItemStack from, ItemStack to) {
        NBTTagCompound tag = from.getTagCompound();
        if (tag != null && tag.hasKey(LABEL_TAG, Constants.NBT.TAG_COMPOUND)) {
            if (!to.hasTagCompound()) {
                to.setTagCompound(new NBTTagCompound());
            }
            to.getTagCompound().setTag(LABEL_TAG, tag.getCompoundTag(LABEL_TAG).copy());
        }
    }
}