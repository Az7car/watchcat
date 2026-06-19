package com.az7car.watchcat.core.netty;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TickTimerAnalyzer {

    private static final ConcurrentHashMap<UUID, Deque<Long>> packetIntervals = new ConcurrentHashMap<>();
    private static final int WINDOW_SIZE = 100;

    public static void recordPacket(UUID playerId) {
        var intervals = packetIntervals.computeIfAbsent(playerId, k -> new ConcurrentLinkedDeque<>());
        long now = System.nanoTime();

        Long last = intervals.peekLast();
        if (last != null) {
            long interval = now - last;
            intervals.addLast(now);
        } else {
            intervals.addLast(now);
        }

        while (intervals.size() > WINDOW_SIZE + 1) {
            intervals.pollFirst();
        }
    }

    public static double getIntervalStd(UUID playerId) {
        var intervals = packetIntervals.get(playerId);
        if (intervals == null || intervals.size() < 10) return 0;

        var list = new ArrayList<>(intervals);
        long[] diffs = new long[list.size() - 1];
        for (int i = 1; i < list.size(); i++) {
            diffs[i - 1] = (list.get(i) - list.get(i - 1)) / 1_000_000L;
        }

        double mean = 0;
        for (long d : diffs) mean += d;
        mean /= diffs.length;

        double variance = 0;
        for (long d : diffs) {
            variance += Math.pow(d - mean, 2);
        }
        variance /= diffs.length;

        return Math.sqrt(variance);
    }

    public static double getIntervalMean(UUID playerId) {
        var intervals = packetIntervals.get(playerId);
        if (intervals == null || intervals.size() < 5) return 50;

        var list = new ArrayList<>(intervals);
        long[] diffs = new long[list.size() - 1];
        for (int i = 1; i < list.size(); i++) {
            diffs[i - 1] = (list.get(i) - list.get(i - 1)) / 1_000_000L;
        }
        double mean = 0;
        for (long d : diffs) mean += d;
        return mean / diffs.length;
    }

    public static boolean isTimerManipulated(UUID playerId) {
        double std = getIntervalStd(playerId);
        double mean = getIntervalMean(playerId);
        if (mean < 1) return false;
        double cv = std / mean;
        return cv < 0.1 || cv > 2.0 || mean < 10 || mean > 200;
    }

    public static void clear(UUID playerId) {
        packetIntervals.remove(playerId);
    }
}
