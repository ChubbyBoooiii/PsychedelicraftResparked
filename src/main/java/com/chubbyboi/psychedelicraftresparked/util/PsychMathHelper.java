package com.chubbyboi.psychedelicraftresparked.util;

public class PsychMathHelper {

    // Maps a value from a range [min, max] to [0, 1].
    public static float zeroToOne(float value, float min, float max) {
        return clamp((value - min) / (max - min), 0.0f, 1.0f);
    }


    // Clamps a value between min and max.
    public static float clamp(float value, float min, float max) {
        return value < min ? min : value > max ? max : value;
    }

    // Linear interpolation between two values.
    public static float mix(float a, float b, float t) {
        return a + (b - a) * t;
    }

    // Chase-ease a value towards a target
    public static float nearValue(float value, float dest, float mulSpeed, float plusSpeed) {
        value += (dest - value) * mulSpeed;

        if (value > dest) {
            value -= plusSpeed;
            if (value < dest) value = dest;
        } else if (value < dest) {
            value += plusSpeed;
            if (value > dest) value = dest;
        }

        return value;
    }

    // Blends colorBase towards colour by alpha
    public static void mixColorsDynamic(float[] color, float[] colorBase, float alpha) {
        if (alpha <= 0.0f) return;

        float max = alpha + colorBase[3];
        colorBase[0] = mix(colorBase[0], color[0], alpha / max);
        colorBase[1] = mix(colorBase[1], color[1], alpha / max);
        colorBase[2] = mix(colorBase[2], color[2], alpha / max);
        colorBase[3] = max;
    }
}