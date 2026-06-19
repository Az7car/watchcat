package com.az7car.watchcat.punishment;

public class ConfidenceTracker {

    private final long uuid;
    private double confidence;
    private long lastUpdateTick;
    private static final double DECAY_PER_TICK = 0.995;
    private static final double BOOST_THRESHOLD = 0.3;
    private static final double DECAY_ACCELERATION = 0.98;

    public ConfidenceTracker() {
        this.confidence = 0.0;
        this.lastUpdateTick = 0;
    }

    public void update(double delta, long currentTick) {
        decay(currentTick);
        this.confidence = Math.min(1.0, Math.max(0.0, this.confidence + delta));
        this.lastUpdateTick = currentTick;
    }

    public void decay(long currentTick) {
        if (lastUpdateTick <= 0) {
            lastUpdateTick = currentTick;
            return;
        }

        long ticksSinceUpdate = Math.max(1, currentTick - lastUpdateTick);
        if (ticksSinceUpdate <= 1) return;

        double decayRate = DECAY_PER_TICK;
        if (confidence < BOOST_THRESHOLD) {
            decayRate = DECAY_ACCELERATION;
        }

        for (long i = 0; i < ticksSinceUpdate && i < 100; i++) {
            confidence *= decayRate;
        }

        if (confidence < 0.001) confidence = 0;
        lastUpdateTick = currentTick;
    }

    public double getConfidence(long currentTick) {
        decay(currentTick);
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = Math.min(1.0, Math.max(0.0, confidence));
    }

    public boolean isConfirmed(double threshold) {
        return confidence >= threshold;
    }

    public void reset() {
        this.confidence = 0.0;
    }
}
