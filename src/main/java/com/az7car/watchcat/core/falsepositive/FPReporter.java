package com.az7car.watchcat.core.falsepositive;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FPReporter {

    private static FPReporter instance;
    private final Map<String, Integer> reportCounts = new ConcurrentHashMap<>();
    private final Map<String, Set<UUID>> reporters = new ConcurrentHashMap<>();
    private static final int AUTO_WHITELIST_THRESHOLD = 10;

    public FPReporter() {
        instance = this;
    }

    public static FPReporter getInstance() { return instance; }

    public void reportFalsePositive(UUID reporter, String checkName) {
        reporters.computeIfAbsent(checkName, k -> ConcurrentHashMap.newKeySet()).add(reporter);
        int count = reportCounts.merge(checkName, 1, Integer::sum);

        if (count >= AUTO_WHITELIST_THRESHOLD) {
            FPWhitelist.getInstance().addGlobalWhitelist(checkName);
        }
    }

    public int getReportCount(String checkName) {
        return reportCounts.getOrDefault(checkName, 0);
    }

    public boolean isReported(String checkName) {
        return reportCounts.getOrDefault(checkName, 0) >= 3;
    }

    public Set<UUID> getReporters(String checkName) {
        return reporters.getOrDefault(checkName, Collections.emptySet());
    }

    public Map<String, Integer> getAllReports() {
        return new HashMap<>(reportCounts);
    }
}
