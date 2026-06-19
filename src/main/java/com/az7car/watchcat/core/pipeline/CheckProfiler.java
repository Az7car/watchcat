package com.az7car.watchcat.core.pipeline;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CheckProfiler {

    private static final ConcurrentHashMap<String, TimingStats> stats = new ConcurrentHashMap<>();
    private static final Map<String, Double> worstChecks = new LinkedHashMap<>();

    public static class TimingStats {
        public long totalTime = 0;
        public int count = 0;
        public long maxTime = 0;

        public double getAverageMicros() {
            return count == 0 ? 0 : (totalTime / (double) count) / 1000.0;
        }

        public double getMaxMicros() {
            return maxTime / 1000.0;
        }
    }

    public static void record(String checkName, long nanos) {
        var s = stats.computeIfAbsent(checkName, k -> new TimingStats());
        s.totalTime += nanos;
        s.count++;
        if (nanos > s.maxTime) s.maxTime = nanos;
    }

    public static Map<String, TimingStats> getAll() { return stats; }

    public static List<Map.Entry<String, TimingStats>> getSlowest(int n) {
        var list = new ArrayList<>(stats.entrySet());
        list.sort((a, b) -> Long.compare(b.getValue().totalTime, a.getValue().totalTime));
        return list.subList(0, Math.min(n, list.size()));
    }

    public static void reset() { stats.clear(); }
}
