package com.az7car.watchcat.core.pipeline;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class FlagAggregator {

    private static final ConcurrentHashMap<UUID, Deque<FlagIncident>> activeIncidents = new ConcurrentHashMap<>();
    private static final long INCIDENT_WINDOW_MS = 10000;
    private static final int MAX_INCIDENTS = 20;

    public static class FlagIncident {
        public final long startTime;
        public final Set<String> checks;
        public final Set<String> categories;
        public int totalFlags;

        public FlagIncident(String checkName, String category) {
            this.startTime = System.currentTimeMillis();
            this.checks = new HashSet<>();
            this.categories = new HashSet<>();
            this.checks.add(checkName);
            this.categories.add(category);
            this.totalFlags = 1;
        }
    }

    public static FlagIncident recordFlag(UUID playerId, String checkName, String category) {
        var incidents = activeIncidents.computeIfAbsent(playerId, k -> new ConcurrentLinkedDeque<>());
        long now = System.currentTimeMillis();

        while (!incidents.isEmpty() && incidents.peekFirst().startTime < now - INCIDENT_WINDOW_MS * 3) {
            incidents.pollFirst();
        }

        FlagIncident current = incidents.peekLast();
        if (current != null && now - current.startTime < INCIDENT_WINDOW_MS) {
            current.checks.add(checkName);
            current.categories.add(category);
            current.totalFlags++;
            return current;
        }

        FlagIncident incident = new FlagIncident(checkName, category);
        incidents.addLast(incident);
        if (incidents.size() > MAX_INCIDENTS) incidents.pollFirst();
        return incident;
    }

    public static int getIncidentCount(UUID playerId) {
        var incidents = activeIncidents.get(playerId);
        return incidents == null ? 0 : incidents.size();
    }

    public static void clear(UUID playerId) {
        activeIncidents.remove(playerId);
    }
}
