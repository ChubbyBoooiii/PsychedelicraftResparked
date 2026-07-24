package com.chubbyboi.psychedelicraftresparked.drug.drugs;

import com.chubbyboi.psychedelicraftresparked.drug.DrugBase;

public class DrugCaffeine extends DrugBase {

    public DrugCaffeine() {
        super(1.0, 0.0002);
    }

    @Override
    public String getName() {
        return "caffeine";
    }

    @Override
    public float getSpeedModifier() {
        return 1.0f + getActiveValue() * 0.2f;
    }

    @Override
    public float getDigSpeedModifier() {
        return getSpeedModifier();
    }

    @Override
    public boolean isSleepBlocked() {
        return getActiveValue() > 0.1f;
    }

    @Override
    public float getRandomJumpChance() {
        return zeroToOne(getActiveValue(), 0.6f, 1.0f) * 0.07f;
    }

    @Override
    public float getRandomPunchChance() {
        return zeroToOne(getActiveValue(), 0.3f, 1.0f) * 0.05f;
    }

    @Override
    public float getHeartbeatVolume() {
        return zeroToOne(getActiveValue(), 0.6f, 1.0f);
    }

    @Override
    public float getHeartbeatSpeed() {
        return getActiveValue() * 0.2f;
    }

    @Override
    public float getBreathVolume() {
        return zeroToOne(getActiveValue(), 0.4f, 1.0f) * 0.5f;
    }

    @Override
    public float getBreathSpeed() {
        return getActiveValue() * 0.3f;
    }

    @Override
    public float getColourSaturationModifier() {
        return getActiveValue() * 0.3f;
    }

    @Override
    public float getHandTrembleStrength() {
        return zeroToOne(getActiveValue(), 0.6f, 1.0f);
    }

    @Override
    public float getViewTrembleStrength() {
        return zeroToOne(getActiveValue(), 0.8f, 1.0f);
    }

    @Override
    public float getColorHallucinationStrength() {
        return zeroToOne(getActiveValue() * 1.3f, 0.7f, 1.0f) * 0.03f;
    }

    @Override
    public float getMovementHallucinationStrength() {
        return zeroToOne(getActiveValue() * 1.3f, 0.7f, 1.0f) * 0.03f;
    }

    @Override
    public float getContextualHallucinationStrength() {
        return zeroToOne(getActiveValue() * 1.3f, 0.7f, 1.0f) * 0.05f;
    }
}
