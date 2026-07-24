package com.chubbyboi.psychedelicraftresparked.drug.drugs;

import com.chubbyboi.psychedelicraftresparked.drug.DrugBase;

public class DrugRedShrooms extends DrugBase {

    public DrugRedShrooms() {
        super(1.0, 0.0002);
    }

    @Override
    public String getName() {
        return "redshrooms";
    }

    @Override
    public float getViewWobblyness() {
        return getActiveValue() * 0.03f;
    }

    @Override
    public float getColorHallucinationStrength() {
        return getActiveValue() * 1.3f;
    }

    @Override
    public float getMovementHallucinationStrength() {
        return getActiveValue() * 0.7f;
    }

    @Override
    public float getContextualHallucinationStrength() {
        return getActiveValue() * 0.2f;
    }
}
