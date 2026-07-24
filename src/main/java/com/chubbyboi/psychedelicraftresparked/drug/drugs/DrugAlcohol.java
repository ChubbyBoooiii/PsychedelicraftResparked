package com.chubbyboi.psychedelicraftresparked.drug.drugs;

import com.chubbyboi.psychedelicraftresparked.drug.DrugBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class DrugAlcohol extends DrugBase {

    private static final DamageSource ALCOHOL_POISONING = new DamageSource("alcoholPoisoning")
        .setDamageBypassesArmor()
        .setDamageIsAbsolute();

    public DrugAlcohol() {
        super(1.0, 0.0002);
    }

    @Override
    public String getName() {
        return "alcohol";
    }

    @Override
    protected void onUpdate(EntityPlayer player, World world, int ticksExisted) {
        if (world.isRemote || getActiveValue() <= 0.001f || ticksExisted % 20 != 0) {
            return;
        }

        double chance = (getActiveValue() - 0.9f) * 2.0f;
        if (player.getRNG().nextFloat() < chance) {
            float damage = (float) (int) ((getActiveValue() - 0.9f) * 50.0f + 4.0f);
            player.attackEntityFrom(ALCOHOL_POISONING, damage);
        }
    }

    @Override
    public float getViewWobblyness() {
        return getActiveValue() * 0.5f;
    }

    @Override
    public float getStumbleStrength() {
        return Math.min(getActiveValue(), 0.8f);
    }

    @Override
    public float getMotionBlurStrength() {
        return zeroToOne(getActiveValue(), 0.5f, 1.0f) * 0.3f;
    }

    @Override
    public float getDoubleVisionStrength() {
        return zeroToOne(getActiveValue(), 0.25f, 1.0f);
    }
}
