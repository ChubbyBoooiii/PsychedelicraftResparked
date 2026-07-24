package com.chubbyboi.psychedelicraftresparked.drug;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.drug.drugs.DrugHarmonium;
import com.chubbyboi.psychedelicraftresparked.util.PsychMathHelper;
import net.minecraft.nbt.NBTTagCompound;

public class DrugInfluenceHarmonium extends DrugInfluence {

    private float[] color;

    public DrugInfluenceHarmonium(String drugName, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence, float[] color) {
        super(drugName, delay, influenceSpeed, influenceSpeedPlus, maxInfluence);
        this.color = color;
    }

    public DrugInfluenceHarmonium() {
        super();
        this.color = new float[3];
    }

    @Override
    public void addToDrug(DrugProperties drugProperties, double value) {
        super.addToDrug(drugProperties, value);

        IDrug drug = drugProperties.getDrug(getDrugName());
        if (drug instanceof DrugHarmonium) {
            DrugHarmonium harmonium = (DrugHarmonium) drug;
            float v = (float) value;
            float inf = v + (1.0f - v) * (1.0f - harmonium.getActiveValue());

            harmonium.currentColor[0] = PsychMathHelper.mix(harmonium.currentColor[0], color[0], inf);
            harmonium.currentColor[1] = PsychMathHelper.mix(harmonium.currentColor[1], color[1], inf);
            harmonium.currentColor[2] = PsychMathHelper.mix(harmonium.currentColor[2], color[2], inf);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setFloat("colorR", color[0]);
        compound.setFloat("colorG", color[1]);
        compound.setFloat("colorB", color[2]);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        color[0] = compound.getFloat("colorR");
        color[1] = compound.getFloat("colorG");
        color[2] = compound.getFloat("colorB");
    }
}
