package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;

public class PluginDetector extends AbstractCheck {

    private static final List<String> SCREENSHARE_PLUGINS = Arrays.asList(
        "screenshare", "ss", "evidence", "capture", "record",
        "investigate", "inspect", "vanished", "vanish",
        "staffmode", "staff", "modmode", "mod",
        "worldeditcui", "worldedit", "worldguard",
        "authme", "loginsecurity", "fastlogin"
    );

    private int suspiciousPluginCount;

    public PluginDetector(WatchcatConfig config) {
        super("PluginDetector", "mod",
            config.getCheckWeight("mod.plugindetector", 0.3),
            config.isCheckEnabled("mod.plugindetector", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundCustomPayloadPacket payload)) return CheckResult.PASS;
        String channel;
        try {
            Object p = payload.getClass().getMethod("payload").invoke(payload);
            Object id = p.getClass().getMethod("type").invoke(p);
            channel = id.toString();
        } catch (Exception e) {
            return CheckResult.PASS;
        }
        if (channel == null) return CheckResult.PASS;
        String lower = channel.toLowerCase();

        for (String plugin : SCREENSHARE_PLUGINS) {
            if (lower.contains(plugin)) {
                suspiciousPluginCount++;
                if (suspiciousPluginCount > 3) {
                    return CheckResult.FLAG;
                }
                return CheckResult.PASS;
            }
        }
        suspiciousPluginCount = Math.max(0, suspiciousPluginCount - 1);
        return CheckResult.PASS;
    }
}
