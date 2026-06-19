package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class AntiBotCheck extends AbstractCheck {

    private final int maxJoinsPerMinute;
    private final int minNameLength;
    private final int maxNameLength;
    private final ConcurrentHashMap<String, JoinTracker> joinTrackers;

    private static final Pattern[] BOT_NAME_PATTERNS = {
        Pattern.compile("^[a-z]{10,}$"),
        Pattern.compile("^[A-Z]{8,}$"),
        Pattern.compile("^[a-zA-Z0-9]{12,}$"),
        Pattern.compile("^[a-zA-Z]+\\d{5,}$"),
        Pattern.compile("^Player\\d{4,}$"),
        Pattern.compile("^\\w{1,2}$"),
        Pattern.compile("^.{15,}$"),
        Pattern.compile("^[a-f0-9]{8,}$"),
    };

    private static class JoinTracker {
        long[] timestamps;
        int count;

        JoinTracker() {
            this.timestamps = new long[60];
            this.count = 0;
        }
    }

    public AntiBotCheck(WatchcatConfig config) {
        super("AntiBot", "mod",
            config.getCheckWeight("mod.antibot", 0.8),
            config.isCheckEnabled("mod.antibot", true));
        this.maxJoinsPerMinute = config.getInt("antibot.max-joins-per-minute", 5);
        this.minNameLength = config.getInt("antibot.min-name-length", 2);
        this.maxNameLength = config.getInt("antibot.max-name-length", 16);
        this.joinTrackers = new ConcurrentHashMap<>();
    }

    public boolean checkJoin(String playerName, String ip) {
        String key = ip != null ? ip : "global";
        JoinTracker tracker = joinTrackers.computeIfAbsent(key, k -> new JoinTracker());
        long now = System.currentTimeMillis();

        tracker.timestamps[tracker.count % 60] = now;
        tracker.count++;

        if (tracker.count >= 60) {
            int recent = 0;
            for (int i = 0; i < 60; i++) {
                if (now - tracker.timestamps[i] < 60000) recent++;
            }
            if (recent > maxJoinsPerMinute) {
                return true;
            }
        }

        if (playerName.length() < minNameLength || playerName.length() > maxNameLength) {
            return true;
        }

        for (Pattern p : BOT_NAME_PATTERNS) {
            if (p.matcher(playerName).matches()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        return CheckResult.PASS;
    }
}
