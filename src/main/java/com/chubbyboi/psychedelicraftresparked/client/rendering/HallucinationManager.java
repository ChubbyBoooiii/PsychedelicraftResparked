package com.chubbyboi.psychedelicraftresparked.client.rendering;

import com.chubbyboi.psychedelicraftresparked.capabilities.DrugProperties;
import com.chubbyboi.psychedelicraftresparked.drug.IDrug;
import com.chubbyboi.psychedelicraftresparked.util.PsychMathHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class HallucinationManager {

    private static final HallucinationManager INSTANCE = new HallucinationManager();

    public static HallucinationManager getInstance() {
        return INSTANCE;
    }

    private HallucinationManager() {
    }

    public enum Pool {
        COLOR, MOVEMENT, CONTEXTUAL
    }

    public enum HallucinationType {
        ENTITIES(Pool.CONTEXTUAL, 0.4f),
        SUPER_SATURATION(Pool.COLOR, 1.0f),
        SLOW_COLOR_ROTATION(Pool.COLOR, 1.0f),
        QUICK_COLOR_ROTATION(Pool.COLOR, 1.0f),
        BIG_WAVES(Pool.MOVEMENT, 0.6f),
        SMALL_WAVES(Pool.MOVEMENT, 0.5f),
        WIGGLE_WAVES(Pool.MOVEMENT, 0.7f),
        PULSES(Pool.COLOR, 1.0f),
        SURFACE_FRACTALS(Pool.COLOR, 1.0f),
        DISTANT_WORLD_DEFORMATION(Pool.MOVEMENT, 1.0f),
        BLOOM(Pool.COLOR, 1.0f),
        COLOR_BLOOM(Pool.COLOR, 1.5f),
        COLOR_CONTRAST(Pool.COLOR, 1.0f);

        public final Pool pool;
        public final float scalar;

        HallucinationType(Pool pool, float scalar) {
            this.pool = pool;
            this.scalar = scalar;
        }
    }

    private static final int CHURN_WINDOW_TICKS = 20 * 60 * 5;

    private final Map<Pool, Float> poolCurrentValue = new EnumMap<>(Pool.class);
    private final Map<HallucinationType, Float> hallucinationValues = new EnumMap<>(HallucinationType.class);
    private final Set<HallucinationType> activeHallucinations = EnumSet.noneOf(HallucinationType.class);
    private final Map<HallucinationType, Float> debugOverrides = new EnumMap<>(HallucinationType.class);

    private final float[] currentMindColor = {1.0f, 1.0f, 1.0f};

    private final int[] mindColorPhaseOffset = new int[3];
    {
        Random seedRandom = new Random();
        for (int i = 0; i < mindColorPhaseOffset.length; i++) {
            mindColorPhaseOffset[i] = seedRandom.nextInt(1_000_000);
        }
    }

    private float[] debugMindColorOverride = null;

    public void update(DrugProperties drugProps, Random random, int ticksExisted) {
        float total = 0.0f;
        for (Pool pool : Pool.values()) {
            float desired = sumPoolContribution(pool, drugProps);
            float current = PsychMathHelper.nearValue(poolCurrentValue.getOrDefault(pool, 0.0f), desired, 0.01f, 0.01f);
            poolCurrentValue.put(pool, current);
            total += current;
        }

        int desiredCount = MathHelper.floor(total * 4.0f + 0.9f);

        if (!activeHallucinations.isEmpty()) {
            while (random.nextFloat() < 1.0f / (CHURN_WINDOW_TICKS / activeHallucinations.size())) {
                removeRandom(random);
                addRandom(random);
            }
        }

        while (activeHallucinations.size() > desiredCount) {
            if (!removeRandom(random)) break;
        }
        while (activeHallucinations.size() < desiredCount) {
            if (!addRandom(random)) break;
        }

        for (HallucinationType type : HallucinationType.values()) {
            float val = hallucinationValues.getOrDefault(type, 0.0f);
            if (activeHallucinations.contains(type)) {
                float target = randomColor(ticksExisted, getMultiplier(type), 0.5f, 0.00121f, 0.0019318f);
                val = PsychMathHelper.nearValue(val, target, 0.002f, 0.002f);
            } else {
                val = PsychMathHelper.nearValue(val, 0.0f, 0.002f, 0.002f);
            }
            hallucinationValues.put(type, val);
        }

        if (debugMindColorOverride != null) {
            currentMindColor[0] = debugMindColorOverride[0];
            currentMindColor[1] = debugMindColorOverride[1];
            currentMindColor[2] = debugMindColorOverride[2];
        } else {
            currentMindColor[0] = PsychMathHelper.nearValue(currentMindColor[0], randomColor(ticksExisted + mindColorPhaseOffset[0], 0.5f, 0.5f, 0.0012371f, 0.0017412f), 0.002f, 0.002f);
            currentMindColor[1] = PsychMathHelper.nearValue(currentMindColor[1], randomColor(ticksExisted + mindColorPhaseOffset[1], 0.5f, 0.5f, 0.0011239f, 0.0019321f), 0.002f, 0.002f);
            currentMindColor[2] = PsychMathHelper.nearValue(currentMindColor[2], randomColor(ticksExisted + mindColorPhaseOffset[2], 0.5f, 0.5f, 0.0011541f, 0.0018682f), 0.002f, 0.002f);
        }
    }

    private float sumPoolContribution(Pool pool, DrugProperties drugProps) {
        float sum = 0.0f;
        for (IDrug drug : drugProps.getAllDrugs()) {
            if (drug.getActiveValue() <= 0.001f) continue;
            switch (pool) {
                case COLOR:
                    sum += drug.getColorHallucinationStrength();
                    break;
                case MOVEMENT:
                    sum += drug.getMovementHallucinationStrength();
                    break;
                case CONTEXTUAL:
                    sum += drug.getContextualHallucinationStrength();
                    break;
            }
        }
        return sum;
    }

    private float getMultiplier(HallucinationType type) {
        return PsychMathHelper.zeroToOne(poolCurrentValue.getOrDefault(type.pool, 0.0f), 0.0f, 0.5f);
    }

    private static float randomColor(int ticksExisted, float base, float sway, float... speeds) {
        for (float speed : speeds) {
            base *= 1.0f + MathHelper.sin(ticksExisted * speed) * sway;
        }
        return base;
    }

    private boolean addRandom(Random random) {
        float maxValue = 0.0f;
        HallucinationType chosen = null;
        for (HallucinationType type : HallucinationType.values()) {
            if (activeHallucinations.contains(type)) continue;
            float value = random.nextFloat() * getMultiplier(type);
            if (value > maxValue) {
                maxValue = value;
                chosen = type;
            }
        }
        if (chosen != null) {
            activeHallucinations.add(chosen);
            return true;
        }
        return false;
    }

    private boolean removeRandom(Random random) {
        if (activeHallucinations.isEmpty()) return false;
        int index = random.nextInt(activeHallucinations.size());
        int i = 0;
        for (HallucinationType type : activeHallucinations) {
            if (i++ == index) {
                activeHallucinations.remove(type);
                return true;
            }
        }
        return false;
    }

    public float getEffectValue(HallucinationType type) {
        Float override = debugOverrides.get(type);
        if (override != null) return override;
        return type.scalar * getMultiplier(type) * hallucinationValues.getOrDefault(type, 0.0f);
    }



    public float getEntityHallucinationStrength() {
        return getEffectValue(HallucinationType.ENTITIES);
    }

    public float getSuperSaturation() {
        return getEffectValue(HallucinationType.SUPER_SATURATION);
    }

    public float getSlowColorRotation() {
        return getEffectValue(HallucinationType.SLOW_COLOR_ROTATION);
    }

    public float getQuickColorRotation() {
        return getEffectValue(HallucinationType.QUICK_COLOR_ROTATION);
    }

    public float getBigWaveStrength() {
        return getEffectValue(HallucinationType.BIG_WAVES);
    }

    public float getSmallWaveStrength() {
        return getEffectValue(HallucinationType.SMALL_WAVES);
    }

    public float getWiggleWaveStrength() {
        return getEffectValue(HallucinationType.WIGGLE_WAVES);
    }

    public float getSurfaceFractalStrength() {
        return getEffectValue(HallucinationType.SURFACE_FRACTALS);
    }

    public float getDistantWorldDeformationStrength() {
        return getEffectValue(HallucinationType.DISTANT_WORLD_DEFORMATION);
    }

    public float getBloom() {
        return getEffectValue(HallucinationType.BLOOM);
    }

    public void getPulseColor(float[] outRgba) {
        float val = getEffectValue(HallucinationType.PULSES);
        outRgba[0] = currentMindColor[0];
        outRgba[1] = currentMindColor[1];
        outRgba[2] = currentMindColor[2];
        outRgba[3] = val;
    }

    public void applyColorBloom(DrugProperties drugProps, float[] bloomColor) {
        for (IDrug drug : drugProps.getAllDrugs()) {
            if (drug.getActiveValue() > 0.001f) drug.applyColorBloom(bloomColor);
        }
        float val = PsychMathHelper.clamp(getEffectValue(HallucinationType.COLOR_BLOOM), 0.0f, 1.0f);
        PsychMathHelper.mixColorsDynamic(currentMindColor, bloomColor, val);
    }

    public void applyContrastColorization(DrugProperties drugProps, float[] contrastColor) {
        for (IDrug drug : drugProps.getAllDrugs()) {
            if (drug.getActiveValue() > 0.001f) drug.applyContrastColorization(contrastColor);
        }
        float val = PsychMathHelper.clamp(getEffectValue(HallucinationType.COLOR_CONTRAST), 0.0f, 1.0f);
        PsychMathHelper.mixColorsDynamic(currentMindColor, contrastColor, val);
    }

    public void setDebugOverride(HallucinationType type, float value) {
        debugOverrides.put(type, value);
    }

    public void clearDebugOverride(HallucinationType type) {
        debugOverrides.remove(type);
    }

    public void clearAllDebugOverrides() {
        debugOverrides.clear();
        debugMindColorOverride = null;
    }

    public float getPoolValue(Pool pool) {
        return poolCurrentValue.getOrDefault(pool, 0.0f);
    }

    public boolean isActive(HallucinationType type) {
        return activeHallucinations.contains(type);
    }

    public boolean isDebugForced(HallucinationType type) {
        return debugOverrides.containsKey(type);
    }

    public float[] getCurrentMindColor() {
        return new float[]{currentMindColor[0], currentMindColor[1], currentMindColor[2]};
    }

    public void setDebugMindColor(float r, float g, float b) {
        debugMindColorOverride = new float[]{r, g, b};
    }

    public void clearDebugMindColor() {
        debugMindColorOverride = null;
    }

    public boolean isMindColorForced() {
        return debugMindColorOverride != null;
    }

    public boolean hasAnyEffect() {
        for (HallucinationType type : HallucinationType.values()) {
            if (Math.abs(getEffectValue(type)) > 0.001f) return true;
        }
        return false;
    }

    public void reset() {
        poolCurrentValue.clear();
        hallucinationValues.clear();
        activeHallucinations.clear();
        currentMindColor[0] = 1.0f;
        currentMindColor[1] = 1.0f;
        currentMindColor[2] = 1.0f;
    }
}
