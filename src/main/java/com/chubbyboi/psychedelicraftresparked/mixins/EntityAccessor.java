package com.chubbyboi.psychedelicraftresparked.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.util.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Random;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("inWater")
    boolean isInWater();

    @Accessor("inWater")
    void setInWater(boolean inWater);

    @Accessor("firstUpdate")
    boolean isFirstUpdate();

    @Accessor("rand")
    Random getRand();

    @Invoker("getSplashSound")
    SoundEvent callGetSplashSound();
}