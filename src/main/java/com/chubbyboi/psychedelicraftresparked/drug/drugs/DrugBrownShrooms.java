package com.chubbyboi.psychedelicraftresparked.drug.drugs;

import com.chubbyboi.psychedelicraftresparked.drug.DrugBase;

public class DrugBrownShrooms extends DrugBase {

    public DrugBrownShrooms() {
        super(1.0, 0.0002);
    }

    @Override
    public String getName() {
        return "brownshrooms";
    }

    @Override
    public float getViewWobblyness() {
        return getActiveValue() * 0.03f;
    }

    @Override
    public float getColorHallucinationStrength() {
        return getActiveValue() * 0.8f;
    }

    @Override
    public float getMovementHallucinationStrength() {
        return getActiveValue() * 1.0f;
    }

    @Override
    public float getContextualHallucinationStrength() {
        return getActiveValue() * 0.35f;
    }
}
