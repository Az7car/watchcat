package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class TimerBalanceCheck extends AbstractCheck {

    private long lastPacketTime;
    private double balance;
    private final double maxBalance;

    public TimerBalanceCheck(WatchcatConfig config) {
        super("TimerBalance", "movement",
            config.getCheckWeight("movement.timerbalance", 0.65),
            config.isCheckEnabled("movement.timerbalance", true));
        this.maxBalance = config.getCheckDouble("movement.timerbalance", "max-balance", 100.0);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;
        long now = System.nanoTime();
        if (lastPacketTime > 0) {
            long delta = now - lastPacketTime;
            double expected = 1_000_000.0 / 20.0;
            balance += (expected - delta) / expected;
            if (balance > maxBalance + 50) {
                return CheckResult.CANCELLED;
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
            double expected = 1_000_000.0 / 20.0;
            balance += (expected - delta) / expected;
            if (balance > maxBalance) {
                return CheckResult.FLAG;
            }
        }
        lastPacketTime = now;
        return CheckResult.PASS;
    }
}
