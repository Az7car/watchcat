package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class MorePacketsCheck extends AbstractCheck {

    private final int maxPacketsPerSecond;
    private final int packetWindow;

    public MorePacketsCheck(WatchcatConfig config) {
        super("MorePackets", "mod",
            config.getCheckWeight("mod.morepackets", 0.6),
            config.isCheckEnabled("mod.morepackets", true));
        this.maxPacketsPerSecond = (int) config.getCheckDouble("mod.morepackets", "max-packets-per-second", 30);
        this.packetWindow = (int) config.getCheckDouble("mod.morepackets", "packet-window", 40);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        data.recordPacket(System.nanoTime());
        var timestamps = data.getPacketTimestamps();
        if (timestamps.size() < 5) return CheckResult.PASS;
        long now = System.nanoTime();
        int count = 0;
        for (long t : timestamps) {
            if (now - t < 1_000_000_000L) count++;
        }
        if (count > maxPacketsPerSecond * 2) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        data.recordPacket(System.nanoTime());
        var timestamps = data.getPacketTimestamps();
        if (timestamps.size() < packetWindow) return CheckResult.PASS;
        long now = System.nanoTime();
        int count = 0;
        for (long t : timestamps) {
            if (now - t < 1_000_000_000L) count++;
        }
        if (count > maxPacketsPerSecond) {
            return CheckResult.FLAG;
        }
        return CheckResult.PASS;
    }
}
