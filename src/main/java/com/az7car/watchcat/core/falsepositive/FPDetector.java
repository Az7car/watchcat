package com.az7car.watchcat.core.falsepositive;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class FPDetector {

    private static FPDetector instance;
    private final Map<UUID, Map<String, Integer>> checkFailCounts = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> totalFlags = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastFlagTime = new ConcurrentHashMap<>();
    private final Set<String> knownFalsePositiveChecks = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Map<String, Integer>> falsePositiveVotes = new ConcurrentHashMap<>();
    private static final int FP_THRESHOLD = 10;
    private static final long FP_WINDOW_MS = 60000;

    public FPDetector() {
        instance = this;
        knownFalsePositiveChecks.addAll(Set.of(
            "Reach", "Velocity", "Timer", "TimerBalance", "KeepSprint"
        ));
    }

    public static FPDetector getInstance() { return instance; }

    public boolean isLikelyFalsePositive(UUID uuid, String checkName) {
        Map<String, Integer> fails = checkFailCounts.get(uuid);
        if (fails == null) return false;
        Integer count = fails.get(checkName);
        if (count == null) return false;
        long now = System.currentTimeMillis();
        Long lastFlag = lastFlagTime.get(uuid);
        if (lastFlag != null && (now - lastFlag) > FP_WINDOW_MS) {
            checkFailCounts.remove(uuid);
            totalFlags.remove(uuid);
            return false;
        }
        return count > FP_THRESHOLD;
    }

    public void recordFlag(UUID uuid, String checkName) {
        checkFailCounts.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
            .merge(checkName, 1, Integer::sum);
        totalFlags.merge(uuid, 1, Integer::sum);
        lastFlagTime.put(uuid, System.currentTimeMillis());
    }

    public void recordFalsePositive(UUID uuid, String checkName) {
        falsePositiveVotes.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
            .merge(checkName, 1, Integer::sum);
        int votes = falsePositiveVotes.get(uuid).getOrDefault(checkName, 0);
        if (votes > 5) {
            knownFalsePositiveChecks.add(checkName);
        }
    }

    public boolean isKnownFP(String checkName) {
        return knownFalsePositiveChecks.contains(checkName);
    }

    public int getTotalFlags(UUID uuid) {
        return totalFlags.getOrDefault(uuid, 0);
    }

    public void reset(UUID uuid) {
        checkFailCounts.remove(uuid);
        totalFlags.remove(uuid);
        lastFlagTime.remove(uuid);
        falsePositiveVotes.remove(uuid);
    }

    public void addKnownFP(String checkName) {
        knownFalsePositiveChecks.add(checkName);
    }
}
