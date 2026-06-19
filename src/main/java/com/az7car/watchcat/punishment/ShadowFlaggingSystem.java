package com.az7car.watchcat.punishment;

import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShadowFlaggingSystem {

    private final ConcurrentHashMap<UUID, ConfidenceTracker> trackers;
    private final ConcurrentHashMap<UUID, String> primaryCheats;
    private final ConcurrentHashMap<UUID, String> primarySeverity;

    public ShadowFlaggingSystem() {
        this.trackers = new ConcurrentHashMap<>();
        this.primaryCheats = new ConcurrentHashMap<>();
        this.primarySeverity = new ConcurrentHashMap<>();
    }

    public ConfidenceTracker getTracker(UUID uuid) {
        return trackers.computeIfAbsent(uuid, k -> new ConfidenceTracker());
    }

    public void flag(UUID uuid, AbstractCheck check, double weight, long currentTick) {
        ConfidenceTracker tracker = getTracker(uuid);
        tracker.update(weight * 0.1, currentTick);

        if (!primaryCheats.containsKey(uuid)) {
            primaryCheats.put(uuid, check.getName());
            primarySeverity.put(uuid, check.getCategory());
        } else {
            double existing = tracker.getConfidence(currentTick);
            if (existing > 0.5) {
                primaryCheats.put(uuid, check.getName());
                primarySeverity.put(uuid, getSeverity(check));
            }
        }
    }

    public double getScore(UUID uuid) {
        ConfidenceTracker t = trackers.get(uuid);
        if (t == null) return 0.0;
        return t.getConfidence(System.currentTimeMillis() / 50);
    }

    public boolean isConfirmed(UUID uuid, double threshold) {
        ConfidenceTracker t = trackers.get(uuid);
        if (t == null) return false;
        return t.isConfirmed(threshold);
    }

    public String getPrimaryCheat(UUID uuid) {
        return primaryCheats.getOrDefault(uuid, "Unknown");
    }

    public String getPrimarySeverity(UUID uuid) {
        return primarySeverity.getOrDefault(uuid, "medium");
    }

    public int getTrackedPlayerCount() { return trackers.size(); }

    public void unload(UUID uuid) {
        trackers.remove(uuid);
        primaryCheats.remove(uuid);
        primarySeverity.remove(uuid);
    }

    private String getSeverity(AbstractCheck check) {
        String name = check.getName().toLowerCase();
        if (name.contains("killaura") || name.contains("reach") || name.contains("fly")
            || name.contains("phase") || name.contains("nuker") || name.contains("hitbox")) {
            return "critical";
        }
        if (name.contains("aim") || name.contains("speed") || name.contains("nofall")
            || name.contains("scaffold") || name.contains("tower") || name.contains("timer")
            || name.contains("blink") || name.contains("criticals") || name.contains("bow")) {
            return "high";
        }
        if (name.contains("jesus") || name.contains("step") || name.contains("highjump")
            || name.contains("longjump") || name.contains("fastbreak") || name.contains("airplace")) {
            return "medium";
        }
        return "low";
    }
}
