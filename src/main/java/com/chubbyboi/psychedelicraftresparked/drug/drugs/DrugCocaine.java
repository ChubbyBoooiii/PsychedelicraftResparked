package com.chubbyboi.psychedelicraftresparked.drug.drugs;

import com.chubbyboi.psychedelicraftresparked.drug.DrugBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.Random;

public class DrugCocaine extends DrugBase {

    private static final DamageSource STROKE = new DamageSource("stroke").setDamageBypassesArmor().setDamageIsAbsolute();
    private static final DamageSource HEART_FAILURE = new DamageSource("heartFailure").setDamageBypassesArmor().setDamageIsAbsolute();
    private static final DamageSource RESPIRATORY_FAILURE = new DamageSource("respiratoryFailure").setDamageBypassesArmor().setDamageIsAbsolute();

    public DrugCocaine() {
        super(1.0, 0.0002);
    }

    @Override
    public String getName() {
        return "cocaine";
    }

    @Override
    protected void onUpdate(EntityPlayer player, World world, int ticksExisted) {
        if (world.isRemote || getActiveValue() <= 0.001f || ticksExisted % 20 != 0) {
            return;
        }

        double chance = (getActiveValue() - 0.8f) * 0.1f;
        Random random = player.getRNG();
        if (random.nextFloat() < chance) {
            DamageSource cause = random.nextFloat() < 0.4f ? STROKE : random.nextFloat() < 0.5f ? HEART_FAILURE : RESPIRATORY_FAILURE;
            player.attackEntityFrom(cause, 1000.0f);
        }
    }

    @Override
    public float getSpeedModifier() {
        float strength = getActiveValue();
        if (strength < 0.001f) return 1.0f;

        return 1.0f + strength * 0.15f;
    }

    @Override
    public float getDigSpeedModifier() {
        return getSpeedModifier();
    }

    @Override
    public boolean isSleepBlocked() {
        return getActiveValue() > 0.4f;
    }

    @Override
    public float getRandomJumpChance() {
        return zeroToOne(getActiveValue(), 0.6f, 1.0f) * 0.03f;
    }

    @Override
    public float getRandomPunchChance() {
        return zeroToOne(getActiveValue(), 0.5f, 1.0f) * 0.02f;
    }

    @Override
    public float getHeartbeatVolume() {
        return zeroToOne(getActiveValue(), 0.4f, 1.0f) * 1.2f;
    }

    @Override
    public float getHeartbeatSpeed() {
        return getActiveValue() * 0.1f;
    }

    @Override
    public float getBreathVolume() {
        return zeroToOne(getActiveValue(), 0.4f, 1.0f) * 1.5f;
    }

    @Override
    public float getBreathSpeed() {
        return getActiveValue() * 0.8f;
    }

    @Override
    public float getHeadMotionInertness() {
        return getActiveValue() * 10.0f;
    }

    @Override
    public float getViewTrembleStrength() {
        return zeroToOne(getActiveValue(), 0.8f, 1.0f);
    }

    @Override
    public float getColourDesaturationModifier() {
        return getActiveValue() * 0.75f;
    }

    @Override
    public float getBloomHallucinationStrength() {
        return zeroToOne(getActiveValue(), 0.0f, 1.0f);
    }

    @Override
    public float getHandTrembleStrength() {
        return zeroToOne(getActiveValue(), 0.6f, 1.0f);
    }

    @Override
    public float getColorHallucinationStrength() {
        return zeroToOne(getActiveValue() * 1.3f, 0.7f, 1.0f) * 0.05f;
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