package com.az7car.watchcat.core.falsepositive;

import com.az7car.watchcat.detection.base.CheckResult;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class VerificationSystem {

    private static VerificationSystem instance;
    private final Map<UUID, Map<String, Integer>> pendingVerifications = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Integer>> confirmedFlags = new ConcurrentHashMap<>();
    private static final int REQUIRED_CONFIRMATIONS = 3;

    public VerificationSystem() {
        instance = this;
    }

    public static VerificationSystem getInstance() { return instance; }

    public boolean shouldFlag(UUID uuid, String checkName, CheckResult result) {
        if (result == CheckResult.PASS) return false;

        Map<String, Integer> pending = pendingVerifications.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        Map<String, Integer> confirmed = confirmedFlags.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());

        if (result == CheckResult.FLAG) {
            int count = pending.merge(checkName, 1, Integer::sum);
            if (count >= REQUIRED_CONFIRMATIONS) {
                pending.put(checkName, 0);
                confirmed.merge(checkName, 1, Integer::sum);
                return true;
            }
            return false;
        }

        if (result == CheckResult.CANCELLED) {
            return true;
        }

        return false;
    }

    public int getConfirmedFlags(UUID uuid, String checkName) {
        return confirmedFlags.getOrDefault(uuid, new ConcurrentHashMap<>())
            .getOrDefault(checkName, 0);
    }

    public void reset(UUID uuid) {
        pendingVerifications.remove(uuid);
        confirmedFlags.remove(uuid);
    }
}
