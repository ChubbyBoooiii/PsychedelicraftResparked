package com.chubbyboi.psychedelicraftresparked.block;

import com.chubbyboi.psychedelicraftresparked.Tags;
import com.chubbyboi.psychedelicraftresparked.init.ItemInit;
import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public enum PlacedContainerType {
    // All sizes in pixels (1/16 block), measured from the container's centre/floor
    //   item          - the held item that can be set down as this container
    //   hitHalfWidth  - half the width of the base square
    //   height        - top of the selection/collision box
    //   fluid:
    //     halfWidth   - half the width of the body's fluid box (keep just inside the glass walls)
    //     bottom      - where the fluid starts, above the floor
    //     maxHeight   - fluid height in the body when full
    //   neck:
    //     maxHeight   - how far up the neck the fluid reaches when full;
    //     halfWidth   - half the width of the neck's fluid box; 0 = no neck (body only)
    //   sound         - place/pick-up sound
    BOTTLE(() -> ItemInit.BOTTLE, 2.0F, 14.5F, 1.99F, 0.01F, 9.98F, 0.99F, 2.25F, SoundType.GLASS);

    private final Supplier<Item> item;
    public final float hitHalfWidth;
    public final float height;
    public final float fluidHalfWidth;
    public final float fluidBottom;
    public final float fluidMaxHeight;
    public final float neckFluidHalfWidth;
    public final float neckFluidMaxHeight;
    public final SoundType soundType;
    public final ResourceLocation model;

    PlacedContainerType(Supplier<Item> item, float hitHalfWidth, float height, float fluidHalfWidth, float fluidBottom, float fluidMaxHeight,
                        float neckFluidHalfWidth, float neckFluidMaxHeight, SoundType soundType) {
        this.item = item;
        this.hitHalfWidth = hitHalfWidth;
        this.height = height;
        this.fluidHalfWidth = fluidHalfWidth;
        this.fluidBottom = fluidBottom;
        this.fluidMaxHeight = fluidMaxHeight;
        this.neckFluidHalfWidth = neckFluidHalfWidth;
        this.neckFluidMaxHeight = neckFluidMaxHeight;
        this.soundType = soundType;
        this.model = new ResourceLocation(Tags.MOD_ID, "block/placed/" + name().toLowerCase());
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