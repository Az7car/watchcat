package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import java.util.UUID;

public class NameSpoofCheck extends AbstractCheck {

    private int nameSpoofCount;

    public NameSpoofCheck(WatchcatConfig config) {
        super("NameSpoof", "mod",
            config.getCheckWeight("mod.namespoof", 0.4),
            config.isCheckEnabled("mod.namespoof", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundCustomPayloadPacket payload)) return CheckResult.PASS;
        String channel;
        try {
            channel = payload.getName();
        } catch (Exception e) { return CheckResult.PASS; }
        if (channel == null) return CheckResult.PASS;

        if (channel.contains("name") || (channel.contains("spoof") && !channel.contains("nospoof"))) {
            nameSpoofCount++;
            if (nameSpoofCount > 2) {
                return CheckResult.FLAG;
            }
        } else {
            nameSpoofCount = Math.max(0, nameSpoofCount - 1);
        }
        return CheckResult.PASS;
    }
}
