package com.az7car.watchcat.core.falsepositive;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CheckCooldown {

    private static CheckCooldown instance;
    private final Map<UUID, Map<String, Long>> lastFlags = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Integer>> flagCounts = new ConcurrentHashMap<>();
    private static final long BASE_COOLDOWN_MS = 2000;
    private static final long MAX_COOLDOWN_MS = 30000;
    private static final int ESCALATION_THRESHOLD = 5;

    public CheckCooldown() {
        instance = this;
    }

    public static CheckCooldown getInstance() { return instance; }

    public boolean isOnCooldown(UUID uuid, String checkName) {
        Map<String, Long> flags = lastFlags.get(uuid);
        if (flags == null) return false;
        Long lastFlag = flags.get(checkName);
        if (lastFlag == null) return false;

        int count = flagCounts.getOrDefault(uuid, new ConcurrentHashMap<>())
            .getOrDefault(checkName, 0);
        long cooldown = Math.min(BASE_COOLDOWN_MS * (1 + count / ESCALATION_THRESHOLD), MAX_COOLDOWN_MS);
        return (System.currentTimeMillis() - lastFlag) < cooldown;
    }

    public void recordFlag(UUID uuid, String checkName) {
        lastFlags.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
            .put(checkName, System.currentTimeMillis());
        flagCounts.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
            .merge(checkName, 1, Integer::sum);
    }

    public int getFlagCount(UUID uuid, String checkName) {
        return flagCounts.getOrDefault(uuid, new ConcurrentHashMap<>())
            .getOrDefault(checkName, 0);
    }

    public void reset(UUID uuid) {
        lastFlags.remove(uuid);
        flagCounts.remove(uuid);
    }
}
