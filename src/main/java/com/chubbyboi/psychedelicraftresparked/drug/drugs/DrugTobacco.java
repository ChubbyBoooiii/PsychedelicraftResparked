package com.chubbyboi.psychedelicraftresparked.drug.drugs;

import com.chubbyboi.psychedelicraftresparked.drug.DrugBase;

public class DrugTobacco extends DrugBase {

    public DrugTobacco() {
        super(1.0, 0.003);
    }

    @Override
    public String getName() {
        return "tobacco";
    }

    @Override
    public float getColourDesaturationModifier() {
        return getActiveValue() * 0.2f;
    }
}
