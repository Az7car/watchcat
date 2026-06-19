package com.az7car.watchcat.core.falsepositive;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FPWhitelist {

    private static FPWhitelist instance;
    private final Map<UUID, Set<String>> playerWhitelist = new ConcurrentHashMap<>();
    private final Set<String> globalWhitelist = ConcurrentHashMap.newKeySet();

    public FPWhitelist() {
        instance = this;
        globalWhitelist.addAll(Set.of(
            "Reach", "Velocity", "Timer", "KeepSprint", "FastFall",
            "Motion", "Prediction", "Collision", "Gravity"
        ));
    }

    public static FPWhitelist getInstance() { return instance; }

    public boolean isWhitelisted(UUID uuid, String checkName) {
        if (globalWhitelist.contains(checkName)) return true;
        Set<String> playerChecks = playerWhitelist.get(uuid);
        return playerChecks != null && playerChecks.contains(checkName);
    }

    public void whitelistPlayer(UUID uuid, String checkName) {
        playerWhitelist.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(checkName);
    }

    public void unwhitelistPlayer(UUID uuid, String checkName) {
        Set<String> checks = playerWhitelist.get(uuid);
        if (checks != null) checks.remove(checkName);
    }

    public void addGlobalWhitelist(String checkName) {
        globalWhitelist.add(checkName);
    }

    public void removeGlobalWhitelist(String checkName) {
        globalWhitelist.remove(checkName);
    }
}
