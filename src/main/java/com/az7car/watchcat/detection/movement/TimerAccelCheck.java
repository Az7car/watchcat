package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class TimerAccelCheck extends AbstractCheck {

    private long lastPacketTime;
    private int burstCount;
    private final long minInterval;

    public TimerAccelCheck(WatchcatConfig config) {
        super("TimerAccel", "movement",
            config.getCheckWeight("movement.timeraccel", 0.6),
            config.isCheckEnabled("movement.timeraccel", true));
        this.minInterval = (long) config.getCheckDouble("movement.timeraccel", "min-interval", 1);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;
        long now = System.nanoTime();
        if (lastPacketTime > 0) {
            long delta = now - lastPacketTime;
            if (delta < 100_000) {
                burstCount++;
                if (burstCount > 10) {
                    return CheckResult.CANCELLED;
                }
            } else {
                burstCount = Math.max(0, burstCount - 1);
            }
        }
        lastPacketTime = now;
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;
        long now = System.nanoTime();
        if (lastPacketTime > 0) {
            long delta = now - lastPacketTime;
            if (delta < 500_000) {
                burstCount++;
                if (burstCount > 15) {
                    return CheckResult.FLAG;
                }
            } else {
                burstCount = Math.max(0, burstCount - 2);
            }
        }
        lastPacketTime = now;
        return CheckResult.PASS;
    }
}
