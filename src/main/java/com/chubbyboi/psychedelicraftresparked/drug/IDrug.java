package com.chubbyboi.psychedelicraftresparked.drug;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public interface IDrug {

    String getName();

    float getActiveValue();

    void setActiveValue(float value);

    float getDesiredValue();

    void setDesiredValue(float value);

    void addToDesiredValue(float amount);

    double getDecaySpeed();

    double getDecaySpeedPlus();

    float getNearValue();

    void setNearValue(float value);



    void update(EntityPlayer player, World world, int ticksExisted);

    void updateValues();



    default float getSpeedModifier() {
        return 1.0f;
    }

    default float getDigSpeedModifier() {
        return 1.0f;
    }

    default boolean isSleepBlocked() {
        return false;
    }



    default float getRandomJumpChance() {
        return 0.0f;
    }

    default float getRandomPunchChance() {
        return 0.0f;
    }



    default float getHeartbeatVolume() {
        return 0.0f;
    }

    default float getHeartbeatSpeed() {
        return 0.0f;
    }

    default float getBreathVolume() {
        return 0.0f;
    }

    default float getBreathSpeed() {
        return 0.0f;
    }

    default float getStumbleStrength() {
        return 0.0f;
    }

    default float getHeadMotionInertness() {
        return 0.0f;
    }

    default float getViewWobblyness() {
        return 0.0f;
    }

    default float getViewTrembleStrength() {
        return 0.0f;
    }

    default float getColourSaturationModifier() {
        return 0.0f;
    }

    default float getColourDesaturationModifier() {
        return 0.0f;
    }

    default float getBloomHallucinationStrength() {
        return 0.0f;
    }

    default float getHandTrembleStrength() {
        return 0.0f;
    }

    default float getMotionBlurStrength() {
        return 0.0f;
    }

    default float getSoundVolumeModifier() {
        return 1.0f;
    }

    default float getDoubleVisionStrength() {
        return 0.0f;
    }

    default float getColorHallucinationStrength() {
        return 0.0f;
    }

    default float getMovementHallucinationStrength() {
        return 0.0f;
    }

    default float getContextualHallucinationStrength() {
        return 0.0f;
    }

    default void applyColorBloom(float[] rgba) { }

    default void applyContrastColorization(float[] rgba) { }

    default void drawOverlays(float partialTicks, int width, int height) { }



    void writeToNBT(NBTTagCompound tag);

    void readFromNBT(NBTTagCompound tag);

    default void writeSyncExtraNBT(NBTTagCompound tag) { }

    default void readSyncExtraNBT(NBTTagCompound tag) { }



    default float zeroToOne(float value, float min, float max) {
        if (value <= min) return 0.0f;
        if (value >= max) return 1.0f;
        return (value - min) / (max - min);
    }
}