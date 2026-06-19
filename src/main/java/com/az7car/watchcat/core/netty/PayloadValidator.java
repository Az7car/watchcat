package com.az7car.watchcat.core.netty;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class PayloadValidator {

    private static final Pattern LEGIT_CHANNEL = Pattern.compile(
        "^(minecraft:|MC|MC|FML|fml).*"
    );
    private static final Set<String> KNOWN_LEGIT = Set.of(
        "minecraft:brand", "minecraft:register", "minecraft:debug",
        "MC|Brand", "MC|AdvCdm", "MC|BSign", "MC|BEdit",
        "MC|PickItem", "MC|TrList", "MC|OpenSign",
        "fml:handshake", "fml:loginwrapper"
    );
    private static final Set<String> SUSPICIOUS_PAYLOADS = Set.of(
        "spoof", "fake", "vanilla", "labymod", "5zig",
        "trigger", "cheat", "hack", "client", "wurst",
        "meteor", "impact", "future"
    );

    private static final ConcurrentHashMap<UUID, Integer> suspiciousCount = new ConcurrentHashMap<>();
    private static final int MAX_SUSPICIOUS = 5;

    public static boolean validate(Packet<?> packet) {
        if (!(packet instanceof ServerboundCustomPayloadPacket payload)) return true;
        String channel;
        try {
            Object p = payload.getClass().getMethod("payload").invoke(payload);
            Object id = p.getClass().getMethod("type").invoke(p);
            channel = id.toString();
        } catch (Exception e) {
            return true;
        }
        if (channel == null) return true;

        if (KNOWN_LEGIT.contains(channel)) return true;
        if (LEGIT_CHANNEL.matcher(channel).matches()) return true;

        String lower = channel.toLowerCase();
        for (String sus : SUSPICIOUS_PAYLOADS) {
            if (lower.contains(sus)) return false;
        }

        int digitCount = 0;
        for (char c : channel.toCharArray()) {
            if (Character.isDigit(c)) digitCount++;
        }
        if (digitCount > channel.length() * 0.5) return false;

        return true;
    }

    public static boolean isPlayerSuspicious(UUID playerId) {
        Integer count = suspiciousCount.get(playerId);
        return count != null && count >= MAX_SUSPICIOUS;
    }

    public static void recordSuspicious(UUID playerId) {
        suspiciousCount.merge(playerId, 1, Integer::sum);
    }

    public static void clear(UUID playerId) {
        suspiciousCount.remove(playerId);
    }
}
