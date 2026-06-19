package com.az7car.watchcat.util;

public final class VectorUtils {

    private VectorUtils() {}

    public static double magnitude(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static double magnitudeSquared(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    public static double dot(double ax, double ay, double az, double bx, double by, double bz) {
        return ax * bx + ay * by + az * bz;
    }

    public static double horizontalMagnitude(double x, double z) {
        return Math.sqrt(x * x + z * z);
    }

    public static double getMotionAngle(double dx, double dz) {
        return Math.toDegrees(Math.atan2(-dx, dz));
    }

    public static double angleDifference(double a, double b) {
        double diff = (b - a) % 360;
        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;
        return Math.abs(diff);
    }

    public static boolean isNearlyZero(double value, double epsilon) {
        return Math.abs(value) < epsilon;
    }
}
