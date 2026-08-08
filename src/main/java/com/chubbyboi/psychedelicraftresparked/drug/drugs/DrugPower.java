package com.chubbyboi.psychedelicraftresparked.drug.drugs;

import com.chubbyboi.psychedelicraftresparked.drug.DrugBase;

public class DrugPower extends DrugBase {

    public DrugPower() {
        super(0.95, 0.0001);
    }

    @Override
    public String getName() {
        return "power";
    }

    @Override
    public float getMotionBlurStrength() {
        return getActiveValue() * 0.3f;
    }

    @Override
    public float getColourDesaturationModifier() {
        return getActiveValue() * 0.75f;
    }

    // TODO: soundVolumeModifier (ambient mute) and drawOverlays (glitch particles + lightning flashes).
}