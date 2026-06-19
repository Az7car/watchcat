package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;

public class AntiAntiCheatCheck extends AbstractCheck {

    private static final List<String> SUSPICIOUS_CHANNELS = Arrays.asList(
        "MC|AntiCheat", "MC|AC", "MC|Bypass", "MC|AntiAC",
        "anticheat", "anti-cheat", "bypass", "anticrash",
        "ac-bypass", "anticheat-bypass", "nocheatplus",
        "ncp", "negativity", "themis", "spartan", "matrix",
        "vulcan", "grimac", "grim", "wither", "karma",
        "advanced-anticheat", "anti", "antibot", "anti_bot"
    );

    private int suspiciousChannelCount;

    public AntiAntiCheatCheck(WatchcatConfig config) {
        super("AntiAntiCheat", "mod",
            config.getCheckWeight("mod.antiantiacheat", 0.6),
            config.isCheckEnabled("mod.antiantiacheat", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundCustomPayloadPacket payload)) return CheckResult.PASS;
        String channel;
        try {
            channel = payload.getName();
        } catch (Exception e) {
            return CheckResult.PASS;
        }
        if (channel == null) return CheckResult.PASS;
        String lower = channel.toLowerCase();

        for (String suspicious : SUSPICIOUS_CHANNELS) {
            if (lower.contains(suspicious)) {
                suspiciousChannelCount++;
                if (suspiciousChannelCount > 2) {
                    return CheckResult.FLAG;
                }
                return CheckResult.PASS;
            }
        }
        suspiciousChannelCount = Math.max(0, suspiciousChannelCount - 1);
        return CheckResult.PASS;
    }
}
