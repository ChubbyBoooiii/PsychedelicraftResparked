package com.chubbyboi.psychedelicraftresparked.client.rendering;

import net.minecraft.util.MouseFilter;

public class SmoothCameraHelper {

    private static SmoothCameraHelper instance;

    public static SmoothCameraHelper getInstance() {
        if (instance == null) {
            instance = new SmoothCameraHelper();
        }
        return instance;
    }

    private final MouseFilter filterX = new MouseFilter();
    private final MouseFilter filterY = new MouseFilter();

    private float pendingYaw = 0.0f;
    private float pendingPitch = 0.0f;
    private float filteredX = 0.0f;
    private float filteredY = 0.0f;
    private float lastPartialTicksX = 0.0f;
    private float lastPartialTicksY = 0.0f;

    public static float speedFromSensitivity(float mouseSensitivity) {
        float f = mouseSensitivity * 0.6f + 0.2f;
        return f * f * f * 8.0f;
    }

    public static float multiplierFromInertness(float inertness) {
        return 1.0f / (1.0f + inertness);
    }

    public void tick(float dragFactor) {
        filteredX = filterX.smooth(pendingYaw, dragFactor);
        filteredY = filterY.smooth(pendingPitch, dragFactor);
        pendingYaw = 0.0f;
        pendingPitch = 0.0f;
        lastPartialTicksX = 0.0f;
        lastPartialTicksY = 0.0f;
    }

    public float applyX(float scaledDelta, float partialTicks) {
        pendingYaw += scaledDelta;
        float progress = partialTicks - lastPartialTicksX;
        lastPartialTicksX = partialTicks;
        return filteredX * progress;
    }

    public float applyY(float scaledDelta, float partialTicks) {
        pendingPitch += scaledDelta;
        float progress = partialTicks - lastPartialTicksY;
        lastPartialTicksY = partialTicks;
        return filteredY * progress;
    }

    public void reset() {
        filterX.reset();
        filterY.reset();
        pendingYaw = 0.0f;
        pendingPitch = 0.0f;
        filteredX = 0.0f;
        filteredY = 0.0f;
        lastPartialTicksX = 0.0f;
        lastPartialTicksY = 0.0f;
    }
}
