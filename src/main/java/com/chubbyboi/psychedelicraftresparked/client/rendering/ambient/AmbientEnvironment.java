package com.chubbyboi.psychedelicraftresparked.client.rendering.ambient;

import com.chubbyboi.psychedelicraftresparked.util.PsychMathHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AmbientEnvironment {

    private static final AmbientEnvironment INSTANCE = new AmbientEnvironment();

    private boolean wasInWater;
    private float currentHeat;

    public static AmbientEnvironment getInstance() {
        return INSTANCE;
    }

    public void update(Entity entity) {
        wasInWater = ActiveRenderInfo.getBlockStateAtEntityViewpoint(entity.world, entity, 1.0f).getMaterial() == Material.WATER;

        BlockPos pos = new BlockPos(entity);
        float newHeat = entity.world.getBiome(pos).getTemperature(pos);

        currentHeat = PsychMathHelper.nearValue(currentHeat, newHeat, 0.01f, 0.01f);
    }

    public float getCurrentHeatDistortion() {
        if (wasInWater)
            return 0.0f;

        return PsychMathHelper.clamp((currentHeat - 1.0f) * 0.0015f, 0.0f, 0.01f);
    }

    public float getCurrentWaterDistortion() {
        return wasInWater ? 0.025f : 0.0f;
    }
}