package com.az7car.watchcat.core.falsepositive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class LagCompensation {

    private static LagCompensation instance;
    private final Map<UUID, Long> lastPacketTimes = new ConcurrentHashMap<>();
    private final Map<UUID, long[]> pingSamples = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> lagSpikeCount = new ConcurrentHashMap<>();
    private static final int MAX_SAMPLES = 20;
    private static final long LAG_SPIKE_THRESHOLD_MS = 2000;
    private static final long MAX_PING_THRESHOLD = 500;

    public LagCompensation() {
        instance = this;
    }

    public static LagCompensation getInstance() { return instance; }

    public void recordPacket(UUID uuid) {
        long now = System.currentTimeMillis();
        Long last = lastPacketTimes.get(uuid);
        if (last != null) {
            long delta = now - last;
            long[] samples = pingSamples.computeIfAbsent(uuid, k -> new long[MAX_SAMPLES]);
            for (int i = MAX_SAMPLES - 1; i > 0; i--) {
                samples[i] = samples[i - 1];
            }
            samples[0] = delta;
            if (delta > LAG_SPIKE_THRESHOLD_MS) {
                lagSpikeCount.merge(uuid, 1, Integer::sum);
            }
        }
        lastPacketTimes.put(uuid, now);
    }

    public long getAveragePing(UUID uuid) {
        long[] samples = pingSamples.get(uuid);
        if (samples == null) return 0;
        long sum = 0, count = 0;
        for (long s : samples) {
            if (s > 0) { sum += s; count++; }
        }
        return count > 0 ? sum / count : 0;
    }

    public boolean isLagSpiking(UUID uuid) {
        return lagSpikeCount.getOrDefault(uuid, 0) > 3;
    }

    public boolean isHighPing(UUID uuid) {
        return getAveragePing(uuid) > MAX_PING_THRESHOLD;
    }

    public boolean shouldSkipCheck(UUID uuid) {
        return isLagSpiking(uuid) || isHighPing(uuid);
    }

    public void reset(UUID uuid) {
        lastPacketTimes.remove(uuid);
        pingSamples.remove(uuid);
        lagSpikeCount.remove(uuid);
    }
}
