package com.az7car.watchcat.detection.base;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ViolationBuffer {

    private final ConcurrentLinkedDeque<Double> violations;
    private final int maxSize;
    private final Runnable onThresholdReached;

    public ViolationBuffer(int maxSize, Runnable onThresholdReached) {
        this.maxSize = maxSize;
        this.onThresholdReached = onThresholdReached;
        this.violations = new ConcurrentLinkedDeque<>();
    }

    public void add(double confidence) {
        violations.addLast(confidence);
        if (violations.size() > maxSize) {
            violations.pollFirst();
        }
    }

    public double getRollingAverage() {
        return violations.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public double getMax() {
        return violations.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);
    }

    public double getVariance() {
        double avg = getRollingAverage();
        return violations.stream()
                .mapToDouble(v -> Math.pow(v - avg, 2))
                .average()
                .orElse(0.0);
    }

    public int size() { return violations.size(); }
    public void clear() { violations.clear(); }
}
