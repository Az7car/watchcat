package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class TimerCheck extends AbstractCheck {

    private final double maxPacketsPerTick;
    private final int packetWindow;

    public TimerCheck(WatchcatConfig config) {
        super("Timer", "mod",
            config.getCheckWeight("mod.timer", 0.7),
            config.isCheckEnabled("mod.timer", true));
        this.maxPacketsPerTick = config.getCheckDouble("mod.timer", "max-packets-per-tick", 1.5);
        this.packetWindow = (int) config.getCheckDouble("mod.timer", "packet-window", 20);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;
        data.recordPacket(System.nanoTime());
        var timestamps = data.getPacketTimestamps();
        if (timestamps.size() < packetWindow) return CheckResult.PASS;
        long now = System.nanoTime();
        long oldest = timestamps.peekFirst();
        if (oldest <= 0) return CheckResult.PASS;
        double elapsedSeconds = (now - oldest) / 1_000_000_000.0;
        if (elapsedSeconds <= 0) return CheckResult.PASS;
        double packetsPerSecond = timestamps.size() / elapsedSeconds;
        double expectedPPS = 20.0;
        double ratio = packetsPerSecond / expectedPPS;
        if (ratio > maxPacketsPerTick * 2) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;
        data.recordPacket(System.nanoTime());
        var timestamps = data.getPacketTimestamps();
        if (timestamps.size() < packetWindow) return CheckResult.PASS;
        long now = System.nanoTime();
        long oldest = timestamps.peekFirst();
        if (oldest <= 0) return CheckResult.PASS;
        double elapsedSeconds = (now - oldest) / 1_000_000_000.0;
        if (elapsedSeconds <= 0) return CheckResult.PASS;
        double packetsPerSecond = timestamps.size() / elapsedSeconds;
        double expectedPPS = 20.0;
        double ratio = packetsPerSecond / expectedPPS;
        if (ratio > maxPacketsPerTick) {
            return CheckResult.FLAG;
        }
        return CheckResult.PASS;
    }
}
