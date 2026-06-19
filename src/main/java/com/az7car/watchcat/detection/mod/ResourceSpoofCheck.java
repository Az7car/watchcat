package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ResourceSpoofCheck extends AbstractCheck {

    private int spoofCount;
    private long lastResourceStatusChange;
    private boolean resourceAccepted;

    public ResourceSpoofCheck(WatchcatConfig config) {
        super("ResourceSpoof", "mod",
            config.getCheckWeight("mod.resourcespoof", 0.4),
            config.isCheckEnabled("mod.resourcespoof", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundCustomPayloadPacket payload)) return CheckResult.PASS;
        String channel;
        try {
            channel = payload.getName();
        } catch (Exception e) { return CheckResult.PASS; }
        if (channel == null) return CheckResult.PASS;

        if (channel.contains("resource") || channel.contains("MC|RPack")) {
            long now = System.currentTimeMillis();
            if (now - lastResourceStatusChange < 1000) {
                spoofCount++;
                if (spoofCount > 3) {
                    return CheckResult.FLAG;
                }
            }
            lastResourceStatusChange = now;
        }
        return CheckResult.PASS;
    }
}
