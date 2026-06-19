package com.az7car.watchcat.core.falsepositive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class TrustFactor {

    private static TrustFactor instance;
    private final Map<UUID, Double> trustScores = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> legitActions = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> flaggedActions = new ConcurrentHashMap<>();
    private static final double INITIAL_TRUST = 1.0;
    private static final double MIN_TRUST = 0.1;
    private static final double FLAG_PENALTY = 0.05;
    private static final double LEGIT_BOOST = 0.01;
    private static final int RECENT_ACTIONS_WINDOW = 100;

    public TrustFactor() {
        instance = this;
    }

    public static TrustFactor getInstance() { return instance; }

    public double getTrust(UUID uuid) {
        return trustScores.getOrDefault(uuid, INITIAL_TRUST);
    }

    public void recordLegitAction(UUID uuid) {
        int actions = legitActions.merge(uuid, 1, Integer::sum);
        double current = trustScores.getOrDefault(uuid, INITIAL_TRUST);
        trustScores.put(uuid, Math.min(1.0, current + LEGIT_BOOST));
        if (actions > RECENT_ACTIONS_WINDOW) {
            legitActions.put(uuid, RECENT_ACTIONS_WINDOW / 2);
            flaggedActions.put(uuid, Math.max(0, flaggedActions.getOrDefault(uuid, 0) - 1));
        }
    }

    public void recordFlag(UUID uuid) {
        int flags = flaggedActions.merge(uuid, 1, Integer::sum);
        double current = trustScores.getOrDefault(uuid, INITIAL_TRUST);
        trustScores.put(uuid, Math.max(MIN_TRUST, current - FLAG_PENALTY));
        if (flags > RECENT_ACTIONS_WINDOW / 2) {
            flaggedActions.put(uuid, RECENT_ACTIONS_WINDOW / 2);
        }
    }

    public double getAdjustedThreshold(UUID uuid, double baseThreshold) {
        double trust = getTrust(uuid);
        return baseThreshold * (1.0 + (1.0 - trust) * 0.5);
    }
}
