package com.chubbyboi.psychedelicraftresparked.capabilities;

public interface IDrugProperties {

    float getDrugStrength(String drugType);

    void setDrugStrength(String drugType, float strength);

    float getDesiredDrugStrength(String drugType);

    float getDecayRate(String drugType);

    void setDecayRate(String drugType, float decayRate);

    void copyFrom(IDrugProperties source);
}