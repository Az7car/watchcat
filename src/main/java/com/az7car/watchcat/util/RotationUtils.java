package com.az7car.watchcat.util;

import com.az7car.watchcat.detection.base.TrackedRotation;
import java.util.Collection;

public final class RotationUtils {

    private RotationUtils() {}

    public static double[] getDeltaYawArray(Collection<TrackedRotation> rotations) {
        return rotations.stream().mapToDouble(r -> (double) r.deltaYaw()).toArray();
    }

    public static double[] getDeltaPitchArray(Collection<TrackedRotation> rotations) {
        return rotations.stream().mapToDouble(r -> (double) r.deltaPitch()).toArray();
    }

    public static double rsquared(double[] deltas) {
        if (deltas.length < 3) return 0;
        int n = deltas.length;
        double[] indices = new double[n];
        for (int i = 0; i < n; i++) indices[i] = i;
        double meanX = MathUtils.mean(indices);
        double meanY = MathUtils.mean(deltas);
        double ssxx = 0, ssyy = 0, ssxy = 0;
        for (int i = 0; i < n; i++) {
            double dx = indices[i] - meanX;
            double dy = deltas[i] - meanY;
            ssxx += dx * dx;
            ssyy += dy * dy;
            ssxy += dx * dy;
        }
        if (ssxx == 0 || ssyy == 0) return 0;
        double r = ssxy / (Math.sqrt(ssxx) * Math.sqrt(ssyy));
        return r * r;
    }

    public static double cinematicFit(Collection<Float[]> deltas) {
        if (deltas.size() < 3) return 0;
        double[] pitchDeltas = new double[deltas.size()];
        int i = 0;
        for (Float[] d : deltas) pitchDeltas[i++] = d[1];
        return rsquared(pitchDeltas);
    }

    public static double[] toDirection(double yaw, double pitch) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double xz = Math.cos(pitchRad);
        return new double[]{
            -xz * Math.sin(yawRad),
            -Math.sin(pitchRad),
            xz * Math.cos(yawRad)
        };
    }

    public static float gcdDelta(float a, float b) {
        if (Math.abs(b) < 0.001f) return a;
        return MathUtils.gcd(Math.abs(a), Math.abs(b));
    }
}
