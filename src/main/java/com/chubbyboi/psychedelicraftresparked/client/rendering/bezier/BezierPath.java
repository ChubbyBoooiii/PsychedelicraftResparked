package com.chubbyboi.psychedelicraftresparked.client.rendering.bezier;

import java.util.ArrayList;
import java.util.List;

// A curve through a list of BezierPoints - resolves an arbitrary 0..1 progress along the whole curve into a position/rotation.
public class BezierPath {

    private final List<BezierPoint> bezierPoints;

    private final List<Double> cachedProgresses = new ArrayList<>();
    private boolean isDirty;

    public BezierPath(List<BezierPoint> bezierPoints) {
        this.bezierPoints = new ArrayList<>(bezierPoints);
        isDirty = true;
    }

    public void buildDistances() {
        isDirty = false;

        cachedProgresses.clear();

        double fullDistance = 0.0;
        List<Double> distances = new ArrayList<>();

        BezierPoint previousPoint = null;
        for (BezierPoint bezierPoint : bezierPoints) {
            if (previousPoint != null) {
                double distance = 0.0;

                int samples = 50;
                for (int i = 0; i < samples; i++) {
                    double[] bezierFrom = previousPoint.getBezierDirectionPointFrom();
                    double[] bezierTo = bezierPoint.getBezierDirectionPointTo();

                    double[] position = BezierVectorMath.cubicMix(previousPoint.position, bezierFrom, bezierTo, bezierPoint.position, (double) i / samples);
                    double[] position1 = BezierVectorMath.cubicMix(previousPoint.position, bezierFrom, bezierTo, bezierPoint.position, (double) (i + 1) / samples);

                    distance += BezierVectorMath.distance(position, position1);
                }

                fullDistance += distance;
                distances.add(distance);
            }

            previousPoint = bezierPoint;
        }

        for (double distance : distances) {
            cachedProgresses.add(distance / fullDistance);
        }
    }

    private BezierPathStep getCachedStep(int leftIndex, int rightIndex, double leftProgress, double rightProgress, double innerProgress) {
        return new BezierPathStep(bezierPoints.get(leftIndex), bezierPoints.get(rightIndex), leftProgress, rightProgress, innerProgress);
    }

    public BezierPathStep getCachedStep(double progress) {
        progress = ((progress % 1.0) + 1.0) % 1.0;
        double curProgress = 0.0;

        for (int i = 1; i < bezierPoints.size(); i++) {
            double distance = cachedProgresses.get(i - 1);

            if ((progress - distance) <= 0.0) {
                return getCachedStep(i - 1, i, curProgress, curProgress + distance, progress / distance);
            }

            progress -= distance;
            curProgress += distance;
        }

        int lastIndex = bezierPoints.size() - 1;
        return getCachedStep(lastIndex - 1, lastIndex, 0.0, 1.0, 1.0);
    }

    private BezierPathStep getCachedStepAfterStep(BezierPathStep cachedStep, double stepSize) {
        return getCachedStep(cachedStep.getBezierPathProgress() + stepSize);
    }

    private double[] getMotion(BezierPathStep from, BezierPathStep to) {
        return BezierVectorMath.sub(to.getPosition(), from.getPosition());
    }

    public double[] getNaturalRotation(BezierPathStep cachedStep, double stepSize) {
        double[] motion = getMotion(cachedStep, getCachedStepAfterStep(cachedStep, stepSize * 0.3));
        double[] spherical = BezierVectorMath.sphericalFromCartesian(motion);

        return new double[]{-spherical[0] / Math.PI * 180.0, spherical[1] / Math.PI * 180.0 + 90.0};
    }

    public boolean isDirty() {
        return isDirty;
    }
}