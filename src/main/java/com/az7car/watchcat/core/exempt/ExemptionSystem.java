package com.az7car.watchcat.core.exempt;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExemptionSystem {

    private static final ConcurrentHashMap<UUID, EnumMap<ExemptionType, Integer>> exemptions = new ConcurrentHashMap<>();
    private static final Set<String> WHITELISTED_BRANDS = Set.of(
        "lunarclient:pm", "badlion:modapi", "badlion:custom",
        "feather:client", "labymod3:main",
        "fabric", "fapi", "forge", "fml",
        "vanilla"
    );

    public static void exempt(UUID playerId, ExemptionType type, int ticks) {
        var map = exemptions.computeIfAbsent(playerId, k -> new EnumMap<>(ExemptionType.class));
        int current = map.getOrDefault(type, 0);
        if (ticks > current) map.put(type, ticks);
    }

    public static void exemptAll(UUID playerId, int ticks) {
        for (ExemptionType t : ExemptionType.values()) {
            exempt(playerId, t, ticks);
        }
    }

    public static boolean isExempt(UUID playerId, ExemptionType type) {
        var map = exemptions.get(playerId);
        if (map == null) return false;
        int ticks = map.getOrDefault(type, 0);
        if (ticks > 0) return true;
        int allTicks = map.getOrDefault(ExemptionType.ALL, 0);
        return allTicks > 0;
    }

    public static void tick(UUID playerId) {
        var map = exemptions.get(playerId);
        if (map == null) return;
        map.entrySet().removeIf(e -> e.getValue() <= 1);
        map.entrySet().forEach(e -> e.setValue(e.getValue() - 1));
        if (map.isEmpty()) exemptions.remove(playerId);
    }

    public static boolean isWhitelistedBrand(String brand) {
        if (brand == null) return false;
        String lower = brand.toLowerCase();
        for (String wb : WHITELISTED_BRANDS) {
            if (lower.contains(wb)) return true;
        }
        return false;
    }

    public static void exemptDamage(UUID playerId) {
        exempt(playerId, ExemptionType.VELOCITY, 8);
        exempt(playerId, ExemptionType.MOVEMENT, 5);
    }

    public static void exemptTeleport(UUID playerId) {
        exempt(playerId, ExemptionType.BOUNDING_BOX, 5);
        exempt(playerId, ExemptionType.MOVEMENT, 5);
        exempt(playerId, ExemptionType.ROTATION, 3);
    }

    public static void exemptLogin(UUID playerId) {
        exemptAll(playerId, 60);
    }

    public static void exemptDimensionSwitch(UUID playerId) {
        exempt(playerId, ExemptionType.MOVEMENT, 20);
        exempt(playerId, ExemptionType.BOUNDING_BOX, 10);
        exempt(playerId, ExemptionType.ROTATION, 5);
    }

    public static void exemptVehicle(UUID playerId) {
        exempt(playerId, ExemptionType.MOVEMENT, 2);
        exempt(playerId, ExemptionType.VEHICLE, 2);
    }

    public static void exemptElytra(UUID playerId) {
        exempt(playerId, ExemptionType.FLY, 2);
        exempt(playerId, ExemptionType.ELYTRA, 2);
    }

    public static void exemptBlockPlace(UUID playerId) {
        exempt(playerId, ExemptionType.PHASE, 2);
    }

    public static void exemptRiptide(UUID playerId) {
        exempt(playerId, ExemptionType.MOVEMENT, 10);
        exempt(playerId, ExemptionType.FLY, 10);
    }

    public static void exemptSlimePiston(UUID playerId) {
        exempt(playerId, ExemptionType.MOVEMENT, 5);
        exempt(playerId, ExemptionType.VELOCITY, 3);
    }

    public static boolean isCheckExempt(UUID playerId, String checkName, String category) {
        switch (category) {
            case "combat":
                if (isExempt(playerId, ExemptionType.ALL)) return true;
                if (checkName.contains("Reach") || checkName.contains("Hitbox") || checkName.contains("ReachMulti")) {
                    return isExempt(playerId, ExemptionType.REACH);
                }
                if (checkName.contains("Killaura") || checkName.contains("Aura") || checkName.contains("Aimbot") || checkName.contains("Aim")) {
                    return isExempt(playerId, ExemptionType.KILLAURA);
                }
                if (checkName.contains("Velocity") || checkName.contains("Knockback")) {
                    return isExempt(playerId, ExemptionType.VELOCITY);
                }
                return false;
            case "movement":
                if (isExempt(playerId, ExemptionType.ALL)) return true;
                if (checkName.contains("Fly") || checkName.contains("Elytra") || checkName.contains("Flight")) {
                    return isExempt(playerId, ExemptionType.FLY) || isExempt(playerId, ExemptionType.ELYTRA);
                }
                if (checkName.contains("Speed") || checkName.contains("Bhop") || checkName.contains("BunnyHop")) {
                    return isExempt(playerId, ExemptionType.SPEED);
                }
                if (checkName.contains("Timer")) {
                    return isExempt(playerId, ExemptionType.TIMER);
                }
                if (checkName.contains("Phase") || checkName.contains("Clip")) {
                    return isExempt(playerId, ExemptionType.PHASE);
                }
                if (checkName.contains("Jesus") || checkName.contains("WaterWalk")) {
                    return isExempt(playerId, ExemptionType.VEHICLE);
                }
                return isExempt(playerId, ExemptionType.MOVEMENT);
            case "world":
                return isExempt(playerId, ExemptionType.ALL);
            case "mod":
                return isExempt(playerId, ExemptionType.ALL);
            default:
                return isExempt(playerId, ExemptionType.ALL);
        }
    }

    public static void clear(UUID playerId) {
        exemptions.remove(playerId);
    }

    public static void cleanup() {
        exemptions.clear();
    }
}
