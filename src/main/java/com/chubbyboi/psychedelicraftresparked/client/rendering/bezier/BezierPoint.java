package com.chubbyboi.psychedelicraftresparked.client.rendering.bezier;

// One control point of a BezierPath - a position, a tangent-direction handle, and a colour/size to interpolate along the curve.
public class BezierPoint {

    public double[] position;
    public double[] bezierDirection;

    public int color;
    public double fontSize;

    public BezierPoint(double[] position, double[] bezierDirection, int color, double fontSize) {
        this.position = position;
        this.bezierDirection = bezierDirection;
        this.color = color;
        this.fontSize = fontSize;
    }

    public double getRed() {
        return ((color >> 16) & 255) / 255.0;
    }

    public double getGreen() {
        return ((color >> 8) & 255) / 255.0;
    }

    public double getBlue() {
        return (color & 255) / 255.0;
    }

    public double[] getBezierDirectionPointTo() {
        double[] result = new double[bezierDirection.length];
        for (int i = 0; i < result.length; i++) result[i] = position[i] - bezierDirection[i];
        return result;
    }

    public double[] getBezierDirectionPointFrom() {
        double[] result = new double[bezierDirection.length];
        for (int i = 0; i < result.length; i++) result[i] = position[i] + bezierDirection[i];
        return result;
    }

    public double getFontSize() {
        return fontSize;
    }
}