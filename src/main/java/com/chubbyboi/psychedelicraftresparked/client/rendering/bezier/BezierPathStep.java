package com.chubbyboi.psychedelicraftresparked.client.rendering.bezier;

// A resolved point somewhere between two BezierPoints along a BezierPath, at a given interpolation progress.
public class BezierPathStep {

    private final BezierPoint leftPoint;
    private final BezierPoint rightPoint;
    private final double innerProgress;

    private final double bezierPathProgress;

    private double[] cachedPosition;

    public BezierPathStep(BezierPoint leftPoint, BezierPoint rightPoint, double leftPointProgress, double rightPointProgress, double innerProgress) {
        this.leftPoint = leftPoint;
        this.rightPoint = rightPoint;
        this.innerProgress = innerProgress;

        this.bezierPathProgress = leftPointProgress + (rightPointProgress - leftPointProgress) * innerProgress;
    }

    public BezierPoint getLeftPoint() {
        return leftPoint;
    }

    public BezierPoint getRightPoint() {
        return rightPoint;
    }

    public double getBezierPathProgress() {
        return bezierPathProgress;
    }

    public double getInnerProgress() {
        return innerProgress;
    }

    public double[] getPosition() {
        if (cachedPosition == null) {
            double[] bezierFrom = leftPoint.getBezierDirectionPointFrom();
            double[] bezierTo = rightPoint.getBezierDirectionPointTo();

            cachedPosition = BezierVectorMath.cubicMix(leftPoint.position, bezierFrom, bezierTo, rightPoint.position, innerProgress);
        }

        return cachedPosition;
    }
}