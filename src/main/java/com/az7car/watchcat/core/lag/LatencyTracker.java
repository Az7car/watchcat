package com.az7car.watchcat.core.lag;

import com.az7car.watchcat.util.TPSMonitor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LatencyTracker {

    private static final ConcurrentHashMap<UUID, Deque<LatencySample>> samples = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Deque<HitboxSnapshot>> hitboxHistory = new ConcurrentHashMap<>();
    private static final int MAX_SAMPLES = 100;
    private static final int MAX_HITBOX_SNAPSHOTS = 50;
    private static final long SAMPLE_WINDOW_MS = 10000;

    private static TPSMonitor tpsMonitor;

    public static void init(TPSMonitor tps) { tpsMonitor = tps; }

    public static class LatencySample {
        public final long time;
        public final long rtt;

        LatencySample(long rtt) { this.time = System.nanoTime(); this.rtt = rtt; }
    }

    public static class HitboxSnapshot {
        public final long time;
        public final double x, y, z;
        public final double width, height;

        HitboxSnapshot(double x, double y, double z, double width, double height) {
            this.time = System.nanoTime();
            this.x = x; this.y = y; this.z = z;
            this.width = width; this.height = height;
        }
    }

    public static void recordSample(UUID playerId, long rtt) {
        var dq = samples.computeIfAbsent(playerId, k -> new ConcurrentLinkedDeque<>());
        dq.addLast(new LatencySample(rtt));
        if (dq.size() > MAX_SAMPLES) dq.pollFirst();
    }

    public static void recordHitbox(UUID playerId, double x, double y, double z, double w, double h) {
        var dq = hitboxHistory.computeIfAbsent(playerId, k -> new ConcurrentLinkedDeque<>());
        dq.addLast(new HitboxSnapshot(x, y, z, w, h));
        if (dq.size() > MAX_HITBOX_SNAPSHOTS) dq.pollFirst();
    }

    public static long getSmoothedPing(UUID playerId) {
        var dq = samples.get(playerId);
        if (dq == null || dq.isEmpty()) return 0;
        long now = System.nanoTime();
        var list = new ArrayList<>(dq);
        list.removeIf(s -> (now - s.time) / 1_000_000 > SAMPLE_WINDOW_MS);
        if (list.isEmpty()) return 0;

        list.sort(Comparator.comparingLong(s -> s.rtt));
        int mid = list.size() / 2;
        return list.get(mid).rtt;
    }

    public static double getPingJitter(UUID playerId) {
        var dq = samples.get(playerId);
        if (dq == null || dq.size() < 5) return 0;
        long now = System.nanoTime();
        var list = new ArrayList<>(dq);
        list.removeIf(s -> (now - s.time) / 1_000_000 > SAMPLE_WINDOW_MS);
        if (list.size() < 5) return 0;
        double mean = list.stream().mapToLong(s -> s.rtt).average().orElse(0);
        double variance = list.stream().mapToDouble(s -> Math.pow(s.rtt - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }

    public static HitboxSnapshot getHitboxAtTime(UUID playerId, long nanoTime) {
        var dq = hitboxHistory.get(playerId);
        if (dq == null || dq.isEmpty()) return null;
        var list = new ArrayList<>(dq);
        HitboxSnapshot best = null;
        long bestDiff = Long.MAX_VALUE;
        for (var h : list) {
            long diff = Math.abs(h.time - nanoTime);
            if (diff < bestDiff) { bestDiff = diff; best = h; }
        }
        return best;
    }

    public static HitboxSnapshot rollbackHitbox(UUID playerId, long clickNanoTime) {
        HitboxSnapshot hb = getHitboxAtTime(playerId, clickNanoTime);
        if (hb != null) return hb;
        var dq = hitboxHistory.get(playerId);
        if (dq == null || dq.isEmpty()) return null;
        return dq.peekLast();
    }

    public static double getAdjustedReach(UUID playerId, double rawReach) {
        long ping = getSmoothedPing(playerId);
        double pingSeconds = ping / 1000.0;
        double velocityCompensation = pingSeconds * 0.5;
        double tpsCompensation = 0;
        if (tpsMonitor != null && tpsMonitor.getAverageTPS() < 18.0) {
            tpsCompensation = (20.0 - tpsMonitor.getAverageTPS()) * 0.05;
        }
        return rawReach - velocityCompensation - tpsCompensation;
    }

    public static boolean isLagSpike(UUID playerId) {
        double jitter = getPingJitter(playerId);
        long ping = getSmoothedPing(playerId);
        return jitter > ping * 0.5 || (tpsMonitor != null && tpsMonitor.getAverageTPS() < 16.0);
    }

    public static void clear(UUID playerId) {
        samples.remove(playerId);
        hitboxHistory.remove(playerId);
    }
}
