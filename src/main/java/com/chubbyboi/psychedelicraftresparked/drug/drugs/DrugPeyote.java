package com.chubbyboi.psychedelicraftresparked.drug.drugs;

import com.chubbyboi.psychedelicraftresparked.drug.DrugBase;

public class DrugPeyote extends DrugBase {

    public DrugPeyote() {
        super(1.0, 0.0002);
    }

    @Override
    public String getName() {
        return "peyote";
    }

    @Override
    public float getColorHallucinationStrength() {
        return getActiveValue() * 0.3f;
    }

    @Override
    public float getContextualHallucinationStrength() {
        return getActiveValue() * 0.6f;
    }
}
