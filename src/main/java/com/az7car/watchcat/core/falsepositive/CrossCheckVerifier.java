package com.az7car.watchcat.core.falsepositive;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class CrossCheckVerifier {

    private static final ConcurrentHashMap<UUID, Deque<FlagEntry>> recentFlags = new ConcurrentHashMap<>();
    private static final long WINDOW_MS = 2000;

    private static class FlagEntry {
        final String checkName;
        final String category;
        final long time;

        FlagEntry(String checkName, String category) {
            this.checkName = checkName;
            this.category = category;
            this.time = System.currentTimeMillis();
        }
    }

    public static boolean shouldConfirm(UUID playerId, String checkName, String category) {
        var flags = recentFlags.computeIfAbsent(playerId, k -> new ConcurrentLinkedDeque<>());
        long now = System.currentTimeMillis();
        flags.addLast(new FlagEntry(checkName, category));

        while (!flags.isEmpty() && flags.peekFirst().time < now - WINDOW_MS) {
            flags.pollFirst();
        }

        Set<String> categories = new HashSet<>();
        Set<String> checks = new HashSet<>();
        int count = 0;
        for (var f : flags) {
            categories.add(f.category);
            checks.add(f.checkName);
            count++;
        }
        return categories.size() >= 2 || checks.size() >= 3 || count >= 5;
    }

    public static void clear(UUID playerId) {
        recentFlags.remove(playerId);
    }
}
