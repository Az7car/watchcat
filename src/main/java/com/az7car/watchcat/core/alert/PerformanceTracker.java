package com.az7car.watchcat.core.alert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceTracker {

    private static PerformanceTracker instance;
    private final Map<String, AtomicLong> checkTimings = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> checkCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> checkTotalTime = new ConcurrentHashMap<>();

    public PerformanceTracker() {
        instance = this;
    }

    public static PerformanceTracker getInstance() {
        return instance;
    }

    public void recordCheck(String checkName, long nanos) {
        checkCounts.computeIfAbsent(checkName, k -> new AtomicLong()).incrementAndGet();
        checkTotalTime.computeIfAbsent(checkName, k -> new AtomicLong()).addAndGet(nanos);
    }

    public double getAverageTime(String checkName) {
        long count = checkCounts.getOrDefault(checkName, new AtomicLong()).get();
        long total = checkTotalTime.getOrDefault(checkName, new AtomicLong()).get();
        return count > 0 ? (double) total / count / 1_000_000.0 : 0;
    }

    public long getTotalCalls(String checkName) {
        return checkCounts.getOrDefault(checkName, new AtomicLong()).get();
    }

    public String getSlowestCheck() {
        String slowest = null;
        double maxAvg = 0;
        for (String name : checkCounts.keySet()) {
            double avg = getAverageTime(name);
            if (avg > maxAvg && getTotalCalls(name) > 100) {
                maxAvg = avg;
                slowest = name;
            }
        }
        return slowest != null ? slowest + " (" + String.format("%.2f", maxAvg) + "ms)" : "none";
    }

    public void reset() {
        checkTimings.clear();
        checkCounts.clear();
        checkTotalTime.clear();
    }
}
