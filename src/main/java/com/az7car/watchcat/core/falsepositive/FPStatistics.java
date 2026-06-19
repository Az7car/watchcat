package com.az7car.watchcat.core.falsepositive;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FPStatistics {

    private static final ConcurrentHashMap<String, FPStats> stats = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Map<String, AtomicInteger>> playerStats = new ConcurrentHashMap<>();

    public static class FPStats {
        public final AtomicInteger totalFlags = new AtomicInteger();
        public final AtomicInteger falsePositives = new AtomicInteger();
        public final AtomicInteger confirmedFlags = new AtomicInteger();

        public double getAccuracy() {
            int total = totalFlags.get();
            if (total == 0) return 1.0;
            return (double) confirmedFlags.get() / total;
        }
    }

    public static void recordFlag(String checkName) {
        stats.computeIfAbsent(checkName, k -> new FPStats()).totalFlags.incrementAndGet();
    }

    public static void recordFP(String checkName, UUID playerId) {
        stats.computeIfAbsent(checkName, k -> new FPStats()).falsePositives.incrementAndGet();
        playerStats.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(checkName, k -> new AtomicInteger()).incrementAndGet();
    }

    public static void recordConfirmed(String checkName) {
        stats.computeIfAbsent(checkName, k -> new FPStats()).confirmedFlags.incrementAndGet();
    }

    public static double getAccuracy(String checkName) {
        var s = stats.get(checkName);
        return s == null ? 1.0 : s.getAccuracy();
    }

    public static int getFPsForPlayer(UUID playerId, String checkName) {
        var p = playerStats.get(playerId);
        if (p == null) return 0;
        var c = p.get(checkName);
        return c == null ? 0 : c.get();
    }

    public static Map<String, FPStats> getAllStats() { return stats; }

    public static void reset() {
        stats.clear();
        playerStats.clear();
    }
}
