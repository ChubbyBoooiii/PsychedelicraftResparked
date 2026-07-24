package com.chubbyboi.psychedelicraftresparked.drug.drugs;

import com.chubbyboi.psychedelicraftresparked.drug.DrugBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class DrugCannabis extends DrugBase {

    public DrugCannabis() {
        super(1.0, 0.0002);
    }

    @Override
    public String getName() {
        return "cannabis";
    }

    @Override
    protected void onUpdate(EntityPlayer player, World world, int ticksExisted) {
        if (getActiveValue() > 0.001f && !world.isRemote) {
            player.addExhaustion(0.03f * getActiveValue());
        }
    }

    @Override
    public float getSpeedModifier() {
        float strength = getActiveValue();
        if (strength < 0.001f) return 1.0f;

        return (1.0f - strength) * 0.5f + 0.5f;
    }

    @Override
    public float getDigSpeedModifier() {
        return getSpeedModifier();
    }

    @Override
    public float getHeadMotionInertness() {
        return getActiveValue() * 8.0f;
    }

    @Override
    public float getViewWobblyness() {
        return getActiveValue() * 0.02f;
    }

    @Override
    public float getColourSaturationModifier() {
        return zeroToOne(getActiveValue(), 0.0f, 0.5f) * 0.3f;
    }

    @Override
    public float getColorHallucinationStrength() {
        return zeroToOne(getActiveValue() * 1.3f, 0.5f, 1.0f) * 0.1f;
    }

    @Override
    public float getMovementHallucinationStrength() {
        return getColorHallucinationStrength();
    }

    @Override
    public float getContextualHallucinationStrength() {
        return getColorHallucinationStrength();
    }
}