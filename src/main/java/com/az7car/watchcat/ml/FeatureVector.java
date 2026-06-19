package com.az7car.watchcat.ml;

import com.az7car.watchcat.detection.base.PlayerData;
import java.util.Deque;

public class FeatureVector {

    private final float[] features;
    private static final int FEATURE_COUNT = 16;

    public FeatureVector() {
        this.features = new float[FEATURE_COUNT];
    }

    public float[] compute(PlayerData data) {
        features[0] = (float) meanRotationDelta(data.getRotationDeltas(), 0);
        features[1] = (float) stdRotationDelta(data.getRotationDeltas(), 0);
        features[2] = (float) meanRotationDelta(data.getRotationDeltas(), 1);
        features[3] = (float) stdRotationDelta(data.getRotationDeltas(), 1);
        features[4] = data.computeGcd();
        features[5] = 0;
        features[6] = (float) clickVariance(data.getClickTimestamps());
        features[7] = (float) clickMean(data.getClickTimestamps());
        features[8] = data.getDeltaX();
        features[9] = data.getDeltaY();
        features[10] = data.getDeltaZ();
        features[11] = (float) data.getHorizontalPositionDelta();
        features[12] = (float) packetRate(data.getPacketTimestamps());
        features[13] = data.getAirTicks();
        features[14] = (float) data.getFallDistance();
        features[15] = (float) (data.isOnGround() ? 1.0 : 0.0);

        return features;
    }

    public float[] getFeatures() { return features; }
    public static int getFeatureCount() { return FEATURE_COUNT; }

    private double meanRotationDelta(java.util.List<Float[]> deltas, int index) {
        if (deltas.isEmpty()) return 0;
        return deltas.stream().mapToDouble(d -> d[index]).average().orElse(0);
    }

    private double stdRotationDelta(java.util.List<Float[]> deltas, int index) {
        if (deltas.size() < 2) return 0;
        double mean = meanRotationDelta(deltas, index);
        return Math.sqrt(deltas.stream()
            .mapToDouble(d -> Math.pow(d[index] - mean, 2))
            .average().orElse(0));
    }

    private double clickMean(Deque<Long> clicks) {
        if (clicks.size() < 2) return 0;
        long now = System.currentTimeMillis();
        return clicks.stream()
            .mapToLong(t -> now - t)
            .filter(t -> t > 0 && t < 5000)
            .average().orElse(0);
    }

    private double clickVariance(Deque<Long> clicks) {
        if (clicks.size() < 3) return 0;
        double mean = clickMean(clicks);
        long now = System.currentTimeMillis();
        return clicks.stream()
            .mapToLong(t -> now - t)
            .filter(t -> t > 0 && t < 5000)
            .mapToDouble(t -> Math.pow(t - mean, 2))
            .average().orElse(0);
    }

    private double packetRate(Deque<Long> timestamps) {
        if (timestamps.size() < 2) return 0;
        long now = System.nanoTime();
        long[] intervals = timestamps.stream()
            .mapToLong(t -> (now - t) / 1_000_000L)
            .filter(t -> t > 0 && t < 5000)
            .toArray();
        if (intervals.length < 2) return 0;
        double mean = 0;
        for (long i : intervals) mean += i;
        mean /= intervals.length;
        return mean > 0 ? 1000.0 / mean : 0;
    }
}
