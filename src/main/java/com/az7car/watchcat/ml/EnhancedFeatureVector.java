package com.az7car.watchcat.ml;

import com.az7car.watchcat.detection.base.PlayerData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class EnhancedFeatureVector {

    private static final int FEATURE_COUNT = 32;
    private final float[] features;

    private static final ConcurrentHashMap<UUID, Deque<Double>> velocityHistory = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Deque<Float>> pitchDeltaHistory = new ConcurrentHashMap<>();
    private static final int HISTORY_SIZE = 20;

    public EnhancedFeatureVector() {
        this.features = new float[FEATURE_COUNT];
    }

    public float[] compute(PlayerData data) {
        UUID uuid = data.getUuid();
        var velHist = velocityHistory.computeIfAbsent(uuid, k -> new ConcurrentLinkedDeque<>());
        var pitchHist = pitchDeltaHistory.computeIfAbsent(uuid, k -> new ConcurrentLinkedDeque<>());

        double hv = data.getHorizontalVelocity();
        velHist.addLast(hv);
        if (velHist.size() > HISTORY_SIZE) velHist.pollFirst();

        float dp = data.getDeltaPitch();
        pitchHist.addLast(dp);
        if (pitchHist.size() > HISTORY_SIZE) pitchHist.pollFirst();

        int i = 0;
        features[i++] = (float) meanRotationDelta(data.getRotationDeltas(), 0);
        features[i++] = (float) stdRotationDelta(data.getRotationDeltas(), 0);
        features[i++] = (float) meanRotationDelta(data.getRotationDeltas(), 1);
        features[i++] = (float) stdRotationDelta(data.getRotationDeltas(), 1);
        features[i++] = data.computeGcd();
        features[i++] = data.getLastGcd();
        features[i++] = (float) clickVariance(data.getClickTimestamps());
        features[i++] = (float) clickMean(data.getClickTimestamps());
        features[i++] = data.getDeltaX();
        features[i++] = data.getDeltaY();
        features[i++] = data.getDeltaZ();
        features[i++] = (float) data.getHorizontalPositionDelta();
        features[i++] = (float) packetRate(data.getPacketTimestamps());
        features[i++] = data.getAirTicks();
        features[i++] = (float) data.getFallDistance();
        features[i++] = (float) (data.isOnGround() ? 1.0 : 0.0);

        features[i++] = (float) velocityStd(velHist);
        features[i++] = (float) velocityMean(velHist);
        features[i++] = (float) velocityTrend(velHist);
        features[i++] = (float) pitchDeltaStd(pitchHist);
        features[i++] = (float) pitchDeltaMean(pitchHist);
        features[i++] = (float) pitchDeltaEntropy(pitchHist);

        features[i++] = (float) gcdSequenceStd(data);
        features[i++] = (float) rotationConvergence(data);

        double hPosDelta = data.getHorizontalPositionDelta();
        double hVel = data.getHorizontalVelocity();
        features[i++] = hPosDelta > 0 ? (float) (hVel / hPosDelta) : 0;
        features[i++] = (float) data.getPositionDelta();

        features[i++] = data.getHealth();
        features[i++] = data.getTick();
        features[i++] = data.cancelledPackets;
        features[i++] = data.getSwingCount();
        features[i++] = data.getMlAnomalyScore();

        return features;
    }

    public static int getFeatureCount() { return FEATURE_COUNT; }

    private double meanRotationDelta(List<Float[]> deltas, int index) {
        if (deltas.isEmpty()) return 0;
        return deltas.stream().mapToDouble(d -> d[index]).average().orElse(0);
    }

    private double stdRotationDelta(List<Float[]> deltas, int index) {
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

    private double velocityStd(Deque<Double> hist) {
        if (hist.size() < 3) return 0;
        double m = hist.stream().mapToDouble(d -> d).average().orElse(0);
        return Math.sqrt(hist.stream().mapToDouble(d -> Math.pow(d - m, 2)).average().orElse(0));
    }

    private double velocityMean(Deque<Double> hist) {
        if (hist.isEmpty()) return 0;
        return hist.stream().mapToDouble(d -> d).average().orElse(0);
    }

    private double velocityTrend(Deque<Double> hist) {
        if (hist.size() < 4) return 0;
        var list = new ArrayList<>(hist);
        int n = list.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int j = 0; j < n; j++) {
            sumX += j; sumY += list.get(j);
            sumXY += j * list.get(j); sumX2 += j * j;
        }
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope;
    }

    private double pitchDeltaStd(Deque<Float> hist) {
        if (hist.size() < 3) return 0;
        double m = hist.stream().mapToDouble(d -> d).average().orElse(0);
        return Math.sqrt(hist.stream().mapToDouble(d -> Math.pow(d - m, 2)).average().orElse(0));
    }

    private double pitchDeltaMean(Deque<Float> hist) {
        if (hist.isEmpty()) return 0;
        return hist.stream().mapToDouble(d -> d).average().orElse(0);
    }

    private double pitchDeltaEntropy(Deque<Float> hist) {
        if (hist.size() < 4) return 0;
        var list = new ArrayList<>(hist);
        int bins = 10;
        double min = list.stream().mapToDouble(d -> d).min().orElse(0);
        double max = list.stream().mapToDouble(d -> d).max().orElse(0);
        if (max - min < 0.001) return 0;
        double binSize = (max - min) / bins;
        int[] counts = new int[bins];
        for (float v : list) {
            int idx = Math.min(bins - 1, (int) ((v - min) / binSize));
            counts[idx]++;
        }
        double entropy = 0;
        int n = list.size();
        for (int c : counts) {
            if (c > 0) {
                double p = (double) c / n;
                entropy -= p * Math.log(p);
            }
        }
        return entropy;
    }

    private double gcdSequenceStd(PlayerData data) {
        var rots = data.getRotationBuffer();
        if (rots.size() < 5) return 0;
        double m = rots.stream().mapToDouble(r -> r.gcd).average().orElse(0);
        return Math.sqrt(rots.stream().mapToDouble(r -> Math.pow(r.gcd - m, 2)).average().orElse(0));
    }

    private double rotationConvergence(PlayerData data) {
        var rots = data.getRotationBuffer();
        if (rots.size() < 5) return 0;
        var list = new ArrayList<>(rots);
        double total = 0;
        for (int i = 1; i < list.size(); i++) {
            total += Math.abs(list.get(i).deltaPitch - list.get(i - 1).deltaPitch);
        }
        return total / list.size();
    }
}
