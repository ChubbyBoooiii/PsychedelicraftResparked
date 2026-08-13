package com.chubbyboi.psychedelicraftresparked.client.rendering.bezier;

import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

// Builds the two spiral shapes the Rift Jar's beam text travels along.
public class BezierPathCreator {

    public static BezierPath createSpiraledBezierPath(double distCenter, double heightDistInf, double spirals, double[] endPos, double fontSize, double heightFontInf) {
        ArrayList<BezierPoint> bezierPoints = new ArrayList<>();

        for (double height = 0; height <= endPos[1]; height += endPos[1] / spirals) {
            double height0 = height;
            double height1 = height + (endPos[1] / spirals) * 0.25;
            double height2 = height + (endPos[1] / spirals) * 0.5;
            double height3 = height + (endPos[1] / spirals) * 0.75;

            double x0 = height0 / endPos[1] * endPos[0];
            double x1 = height1 / endPos[1] * endPos[0];
            double x2 = height2 / endPos[1] * endPos[0];
            double x3 = height3 / endPos[1] * endPos[0];

            double z0 = height0 / endPos[1] * endPos[2];
            double z1 = height1 / endPos[1] * endPos[2];
            double z2 = height2 / endPos[1] * endPos[2];
            double z3 = height3 / endPos[1] * endPos[2];

            double distance0 = distCenter * (1.0 + height0 * heightDistInf);
            double distance1 = distCenter * (1.0 + height1 * heightDistInf);
            double distance2 = distCenter * (1.0 + height2 * heightDistInf);
            double distance3 = distCenter * (1.0 + height3 * heightDistInf);

            bezierPoints.add(new BezierPoint(new double[]{distance0 + x0, height0, -distance0 + z0}, new double[]{-0.5 * distance0, 0.0, -0.5 * distance0}, 0xffffffff, fontSize + height0 / endPos[1] * heightFontInf));
            if (height1 <= endPos[1]) {
                bezierPoints.add(new BezierPoint(new double[]{-distance1 + x1, height1, -distance1 + z1}, new double[]{-0.5 * distance1, 0.0, 0.5 * distance1}, 0xffffffff, fontSize + height1 / endPos[1] * heightFontInf));
            }
            if (height2 <= endPos[1]) {
                bezierPoints.add(new BezierPoint(new double[]{-distance2 + x2, height2, distance2 + z2}, new double[]{0.5 * distance2, 0.0, 0.5 * distance2}, 0xffffffff, fontSize + height2 / endPos[1] * heightFontInf));
            }
            if (height3 <= endPos[1]) {
                bezierPoints.add(new BezierPoint(new double[]{distance3 + x3, height3, distance3 + z3}, new double[]{0.5 * distance3, 0.0, -0.5 * distance3}, 0xffffffff, fontSize + height3 / endPos[1] * heightFontInf));
            }
        }

        return new BezierPath(bezierPoints);
    }

    public static BezierPath createSpiraledSphere(double radius, double spirals, double fontSize) {
        ArrayList<BezierPoint> bezierPoints = new ArrayList<>();

        double centerYShift = -radius * 0.5;
        for (double height = 0; height <= radius; height += radius / spirals) {
            double height0 = height;
            double height1 = height + (radius / spirals) * 0.25;
            double height2 = height + (radius / spirals) * 0.5;
            double height3 = height + (radius / spirals) * 0.75;

            double distance0 = MathHelper.cos((float) (height0 / radius - 0.5) * 3.1415926f);
            double distance1 = MathHelper.cos((float) (height1 / radius - 0.5) * 3.1415926f);
            double distance2 = MathHelper.cos((float) (height2 / radius - 0.5) * 3.1415926f);
            double distance3 = MathHelper.cos((float) (height3 / radius - 0.5) * 3.1415926f);

            bezierPoints.add(new BezierPoint(new double[]{distance0, height0 + centerYShift, -distance0}, new double[]{-0.5 * distance0, 0.0, -0.5 * distance0}, 0xffffffff, fontSize));
            if (height1 <= radius) {
                bezierPoints.add(new BezierPoint(new double[]{-distance1, height1 + centerYShift, -distance1}, new double[]{-0.5 * distance1, 0.0, 0.5 * distance1}, 0xffffffff, fontSize));
            }
            if (height2 <= radius) {
                bezierPoints.add(new BezierPoint(new double[]{-distance2, height2 + centerYShift, distance2}, new double[]{0.5 * distance2, 0.0, 0.5 * distance2}, 0xffffffff, fontSize));
            }
            if (height3 <= radius) {
                bezierPoints.add(new BezierPoint(new double[]{distance3, height3 + centerYShift, distance3}, new double[]{0.5 * distance3, 0.0, -0.5 * distance3}, 0xffffffff, fontSize));
            }
        }

        return new BezierPath(bezierPoints);
    }
}