package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public enum PlacedContainerType {
    BOTTLE(() -> ItemInit.BOTTLE, SoundType.GLASS, "wine_bottle", "round_bottle"),
    SHOT_GLASS(() -> ItemInit.SHOT_GLASS, SoundType.GLASS, "shot_glass"),
    GLASS_CHALICE(() -> ItemInit.GLASS_CHALICE, SoundType.GLASS, "glass_chalice"),
    WOODEN_MUG(() -> ItemInit.WOODEN_MUG, SoundType.WOOD, "wooden_mug");

    public static final String SHAPE_TAG = "Shape";

    private final Supplier<Item> item;
    public final SoundType soundType;
    private final List<String> shapeNames;

    PlacedContainerType(Supplier<Item> item, SoundType soundType, String... shapeNames) {
        this.item = item;
        this.soundType = soundType;
        this.shapeNames = Collections.unmodifiableList(Arrays.asList(shapeNames));
    }

    public List<ContainerShape> getShapes() {
        List<ContainerShape> shapes = new ArrayList<>();
        for (String name : shapeNames) {
            shapes.add(ContainerShape.get(name));
        }
        return shapes;
    }

    public ContainerShape getShape(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(SHAPE_TAG, Constants.NBT.TAG_STRING) && shapeNames.contains(tag.getString(SHAPE_TAG))) {
            return ContainerShape.get(tag.getString(SHAPE_TAG));
        }
        return ContainerShape.get(shapeNames.get(0));
    }

    public static ItemStack withShape(ItemStack stack, String shape) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setString(SHAPE_TAG, shape);
        return stack;
    }

    @Nullable
    public static PlacedContainerType of(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (PlacedContainerType type : values()) {
            if (type.item.get() == stack.getItem()) {
                return type;
            }
        }
        return null;
    }
}