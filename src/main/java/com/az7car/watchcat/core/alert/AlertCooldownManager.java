package com.az7car.watchcat.core.alert;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AlertCooldownManager {

    private static final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private static final long DEFAULT_COOLDOWN_MS = 5000;

    public static boolean canAlert(UUID playerId, String checkName) {
        return canAlert(playerId, checkName, DEFAULT_COOLDOWN_MS);
    }

    public static boolean canAlert(UUID playerId, String checkName, long cooldownMs) {
        var playerCooldowns = cooldowns.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        long now = System.currentTimeMillis();
        Long lastAlert = playerCooldowns.get(checkName);
        if (lastAlert != null && now - lastAlert < cooldownMs) {
            return false;
        }
        playerCooldowns.put(checkName, now);
        return true;
    }

    public static void clear(UUID playerId) {
        cooldowns.remove(playerId);
    }
}
