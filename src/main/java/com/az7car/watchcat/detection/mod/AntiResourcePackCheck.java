package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AntiResourcePackCheck extends AbstractCheck {

    private int antiResourceCount;
    private boolean serverHasPack;

    public AntiResourcePackCheck(WatchcatConfig config) {
        super("AntiResourcePack", "mod",
            config.getCheckWeight("mod.antiresourcepack", 0.35),
            config.isCheckEnabled("mod.antiresourcepack", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundCustomPayloadPacket payload)) return CheckResult.PASS;
        String channel;
        try {
            Object p = payload.getClass().getMethod("payload").invoke(payload);
            Object id = p.getClass().getMethod("type").invoke(p);
            channel = id.toString();
        } catch (Exception e) { return CheckResult.PASS; }
        if (channel == null) return CheckResult.PASS;
        String lower = channel.toLowerCase();
        if (lower.contains("resource") && lower.contains("reject")) {
            antiResourceCount++;
            if (antiResourceCount > 2) {
                return CheckResult.FLAG;
            }
        } else {
            antiResourceCount = Math.max(0, antiResourceCount - 1);
        }
        return CheckResult.PASS;
    }
}
