package com.az7car.watchcat.util;

public final class MathUtils {

    private MathUtils() {}

    public static float gcd(float a, float b) {
        if (b < 0.001f) return a;
        return gcd(b, a % b);
    }

    public static float normalizeAngle(float angle) {
        angle %= 360.0f;
        if (angle > 180.0f) angle -= 360.0f;
        if (angle < -180.0f) angle += 360.0f;
        return angle;
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double mean(double[] values) {
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    public static double stdDev(double[] values, double mean) {
        double sum = 0;
        for (double v : values) sum += Math.pow(v - mean, 2);
        return Math.sqrt(sum / values.length);
    }

    public static double stdDev(double[] values) {
        return stdDev(values, mean(values));
    }

    public static double[] toPrimitive(java.util.Collection<Double> collection) {
        return collection.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public static float[] floatListToArray(java.util.Collection<Float> collection) {
        return collection.stream().mapToFloat(Float::floatValue).toArray();
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static boolean approxEquals(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }
}
