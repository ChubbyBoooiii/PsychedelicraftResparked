package com.chubbyboi.psychedelicraftresparked.init;

import com.chubbyboi.psychedelicraftresparked.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class SoundInit {

    public static final SoundEvent HEARTBEAT = create("heartbeat");
    public static final SoundEvent BREATH = create("breath");

    private static SoundEvent create(String name) {
        ResourceLocation id = new ResourceLocation(Tags.MOD_ID, name);
        return new SoundEvent(id).setRegistryName(id);
    }
}
