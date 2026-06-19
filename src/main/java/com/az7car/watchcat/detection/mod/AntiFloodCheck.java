package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.punishment.ChatPunishmentManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class AntiFloodCheck extends AbstractCheck {

    private final ChatPunishmentManager punishmentManager;
    private final int maxMessagesPerSecond;
    private final int maxRepeatedMessages;
    private final int maxCapsRatio;

    private final java.util.concurrent.ConcurrentHashMap<java.util.UUID, FloodTracker> trackers;

    private static class FloodTracker {
        long windowStart;
        int messageCount;
        String lastMessage;
        int repeatCount;

        FloodTracker() {
            this.windowStart = System.currentTimeMillis();
        }
    }

    public AntiFloodCheck(WatchcatConfig config, ChatPunishmentManager punishmentManager) {
        super("AntiFlood", "mod",
            config.getCheckWeight("mod.antiflood", 0.5),
            config.isCheckEnabled("mod.antiflood", true));
        this.punishmentManager = punishmentManager;
        this.maxMessagesPerSecond = config.getInt("chat.max-messages-per-second", 4);
        this.maxRepeatedMessages = config.getInt("chat.max-repeated-messages", 3);
        this.maxCapsRatio = config.getInt("chat.max-caps-ratio", 50);
        this.trackers = new java.util.concurrent.ConcurrentHashMap<>();
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundChatPacket chat)) return CheckResult.PASS;

        String message;
        try { message = (String) chat.getClass().getMethod("message").invoke(chat); }
        catch (Exception e) { return CheckResult.PASS; }
        if (message == null || message.isEmpty()) return CheckResult.PASS;

        java.util.UUID uuid = player.getUniqueId();
        FloodTracker tracker = trackers.computeIfAbsent(uuid, k -> new FloodTracker());
        long now = System.currentTimeMillis();

        if (now - tracker.windowStart > 1000) {
            tracker.windowStart = now;
            tracker.messageCount = 0;
        }
        tracker.messageCount++;

        if (tracker.messageCount > maxMessagesPerSecond) {
            ChatPunishmentManager.Result result = punishmentManager.warnPlayer(player,
                "Chat flood detected (" + tracker.messageCount + " msgs/sec)");
            return result == ChatPunishmentManager.Result.BANNED ? CheckResult.FAIL : CheckResult.FLAG;
        }

        if (message.equals(tracker.lastMessage)) {
            tracker.repeatCount++;
            if (tracker.repeatCount > maxRepeatedMessages) {
                ChatPunishmentManager.Result result = punishmentManager.warnPlayer(player,
                    "Repeated messages detected");
                return result == ChatPunishmentManager.Result.BANNED ? CheckResult.FAIL : CheckResult.FLAG;
            }
        } else {
            tracker.repeatCount = 0;
        }
        tracker.lastMessage = message;

        if (message.length() > 10) {
            int caps = 0;
            for (char c : message.toCharArray()) {
                if (Character.isUpperCase(c)) caps++;
            }
            int ratio = (caps * 100) / message.length();
            if (ratio > maxCapsRatio) {
                ChatPunishmentManager.Result result = punishmentManager.warnPlayer(player,
                    "Excessive caps usage (" + ratio + "%)");
                return result == ChatPunishmentManager.Result.BANNED ? CheckResult.FAIL : CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
