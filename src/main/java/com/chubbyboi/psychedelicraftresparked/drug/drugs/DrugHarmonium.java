package com.chubbyboi.psychedelicraftresparked.drug.drugs;

import com.chubbyboi.psychedelicraftresparked.drug.DrugBase;
import com.chubbyboi.psychedelicraftresparked.util.PsychMathHelper;
import net.minecraft.nbt.NBTTagCompound;

public class DrugHarmonium extends DrugBase {

    public final float[] currentColor = {1.0f, 1.0f, 1.0f};

    public DrugHarmonium() {
        super(1.0, 0.0003);
    }

    @Override
    public String getName() {
        return "harmonium";
    }

    @Override
    public void applyContrastColorization(float[] rgba) {
        PsychMathHelper.mixColorsDynamic(currentColor, rgba, getActiveValue());
    }

    @Override
    public void applyColorBloom(float[] rgba) {
        PsychMathHelper.mixColorsDynamic(currentColor, rgba, getActiveValue() * 3.0f);
    }

    @Override
    public float getMovementHallucinationStrength() {
        return getActiveValue() * 0.4f;
    }

    @Override
    public float getContextualHallucinationStrength() {
        return getActiveValue() * 0.3f;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setFloat("currentColorR", currentColor[0]);
        tag.setFloat("currentColorG", currentColor[1]);
        tag.setFloat("currentColorB", currentColor[2]);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        currentColor[0] = tag.getFloat("currentColorR");
        currentColor[1] = tag.getFloat("currentColorG");
        currentColor[2] = tag.getFloat("currentColorB");
    }

    @Override
    public void writeSyncExtraNBT(NBTTagCompound tag) {
        tag.setFloat("currentColorR", currentColor[0]);
        tag.setFloat("currentColorG", currentColor[1]);
        tag.setFloat("currentColorB", currentColor[2]);
    }

    @Override
    public void readSyncExtraNBT(NBTTagCompound tag) {
        currentColor[0] = tag.getFloat("currentColorR");
        currentColor[1] = tag.getFloat("currentColorG");
        currentColor[2] = tag.getFloat("currentColorB");
    }
}
