package com.chubbyboi.psychedelicraftresparked.drug;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class DrugBase implements IDrug {

    // Drug state
    private float activeValue = 0.0f;
    private float desiredValue = 0.0f;
    private float nearValue = 0.0f; // For client-side interpolation

    // Decay parameters (from original)
    private final double decaySpeed;      // Base decay rate
    private final double decaySpeedPlus;  // Additional decay based on current value

    public DrugBase(double decaySpeed, double decaySpeedPlus) {
        this.decaySpeed = decaySpeed;
        this.decaySpeedPlus = decaySpeedPlus;
    }



    @Override
    public float getActiveValue() {
        return activeValue;
    }

    @Override
    public void setActiveValue(float value) {
        this.activeValue = Math.max(0.0f, value);
    }

    @Override
    public float getDesiredValue() {
        return desiredValue;
    }

    @Override
    public void setDesiredValue(float value) {
        this.desiredValue = Math.max(0.0f, value);
    }

    @Override
    public void addToDesiredValue(float amount) {
        this.desiredValue = Math.max(0.0f, this.desiredValue + amount);
    }

    @Override
    public float getNearValue() {
        return nearValue;
    }

    @Override
    public void setNearValue(float value) {
        this.nearValue = Math.max(0.0f, value);
    }

    @Override
    public double getDecaySpeed() {
        return decaySpeed;
    }

    @Override
    public double getDecaySpeedPlus() {
        return decaySpeedPlus;
    }



    @Override
    public void update(EntityPlayer player, World world, int ticksExisted) {
        updateValues();
        onUpdate(player, world, ticksExisted);
    }

    protected void onUpdate(EntityPlayer player, World world, int ticksExisted) {

    }

    @Override
    public void updateValues() {
        if (desiredValue > 0.0) {
            desiredValue *= (float) decaySpeed;           // Multiply first
            desiredValue -= (float) decaySpeedPlus;       // Then subtract
            desiredValue = Math.max(0.0f, desiredValue);  // Clamp to 0
        }

        desiredValue = Math.max(0.0f, Math.min(1.0f, desiredValue));
        float difference = Math.abs(activeValue - desiredValue);
        float step = difference > 0.1f ? 0.05f : 0.005f;

        if (activeValue < desiredValue) {
            activeValue = Math.min(desiredValue, activeValue + step);
        } else if (activeValue > desiredValue) {
            activeValue = Math.max(desiredValue, activeValue - step);
        }

        // Clamp activeValue too (safety)
        activeValue = Math.max(0.0f, Math.min(1.0f, activeValue));
    }



    @Override
    public float getSpeedModifier() {
        return 1.0f; // No effect by default
    }

    @Override
    public float getDigSpeedModifier() {
        return 1.0f; // No effect by default
    }

    @Override
    public float getColourSaturationModifier() {
        return 0.0f; // No saturation effect by default
    }



    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setFloat("activeValue", activeValue);
        tag.setFloat("desiredValue", desiredValue);
        tag.setFloat("nearValue", nearValue);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.activeValue = tag.getFloat("activeValue");
        this.desiredValue = tag.getFloat("desiredValue");
        this.nearValue = tag.getFloat("nearValue");
    }
}