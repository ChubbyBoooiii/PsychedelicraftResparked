package com.chubbyboi.psychedelicraftresparked.client.rendering.bezier;

import com.chubbyboi.psychedelicraftresparked.util.PsychMathHelper;
import net.minecraft.util.math.MathHelper;

// Small double[]-vector helpers the bezier beam curves need (cubic interpolation, distance, cross product)
public class BezierVectorMath {

    public static double length(double[] vector) {
        double lengthSQ = 0.0;
        for (double v : vector) lengthSQ += v * v;
        return MathHelper.sqrt(lengthSQ);
    }

    public static double distance(double[] pos1, double[] pos2) {
        double distanceSQ = 0.0;
        for (int i = 0; i < pos1.length; i++) distanceSQ += (pos1[i] - pos2[i]) * (pos1[i] - pos2[i]);
        return MathHelper.sqrt(distanceSQ);
    }

    public static double[] cubicMix(double[] pos1, double[] pos2, double[] pos3, double[] pos4, double progress) {
        double[] result = new double[pos1.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = PsychMathHelper.cubicMix(pos1[i], pos2[i], pos3[i], pos4[i], progress);
        }
        return result;
    }

    public static double[] sphericalFromCartesian(double[] vector) {
        double r = length(vector);
        double inclination = Math.acos(vector[1] / r);
        double azimuth = Math.atan2(vector[2], vector[0]);
        return new double[]{azimuth, inclination, r};
    }

    public static double[] sub(double[] vector, double[] subVector) {
        double[] result = vector.clone();
        for (int i = 0; i < result.length; i++) result[i] -= subVector[i];
        return result;
    }
}