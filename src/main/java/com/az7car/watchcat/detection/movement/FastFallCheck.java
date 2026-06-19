package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class FastFallCheck extends AbstractCheck {

    private final double maxFallSpeed;
    private int fastFallCount;

    public FastFallCheck(WatchcatConfig config) {
        super("FastFall", "movement",
            config.getCheckWeight("movement.fastfall", 0.6),
            config.isCheckEnabled("movement.fastfall", true));
        this.maxFallSpeed = config.getCheckDouble("movement.fastfall", "max-fall-speed", 0.08);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        if (move.isOnGround()) return CheckResult.PASS;
        double dy = data.getPositionDelta().getY();
        if (dy >= 0) return CheckResult.PASS;

        double currentFall = Math.abs(dy);
        if (currentFall < 0.01) return CheckResult.PASS;

        double prevDy = data.getLastPositionDelta().getY();
        if (prevDy >= 0) return CheckResult.PASS;

        if (currentFall > 0.0785 && currentFall < 0.08) {
            return CheckResult.PASS;
        }

        double deltaFall = currentFall - Math.abs(prevDy);
        if (deltaFall > maxFallSpeed + 0.1) {
            fastFallCount++;
            if (fastFallCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            fastFallCount = Math.max(0, fastFallCount - 1);
        }
        return CheckResult.PASS;
    }
}
