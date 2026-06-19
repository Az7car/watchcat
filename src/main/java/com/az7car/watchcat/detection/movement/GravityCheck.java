package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class GravityCheck extends AbstractCheck {

    private final double minGravity;
    private final double maxGravity;
    private int gravityFailCount;

    public GravityCheck(WatchcatConfig config) {
        super("Gravity", "movement",
            config.getCheckWeight("movement.gravity", 0.65),
            config.isCheckEnabled("movement.gravity", true));
        this.minGravity = config.getCheckDouble("movement.gravity", "min-gravity", 0.08);
        this.maxGravity = config.getCheckDouble("movement.gravity", "max-gravity", 0.12);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        if (move.isOnGround()) return CheckResult.PASS;
        if (player.isInsideVehicle() || player.isFlying()) return CheckResult.PASS;

        double dy = data.getDeltaY();
        if (dy >= 0) return CheckResult.PASS;

        double gravity = Math.abs(dy);
        if (data.wasOnGround()) {
            return CheckResult.PASS;
        }

        double prevDy = data.getLastDeltaY();
        if (prevDy >= 0 || Math.abs(prevDy) < 0.01) return CheckResult.PASS;

        double gravityChange = Math.abs(gravity - Math.abs(prevDy));
        if (dy < prevDy) {
            gravityChange = Math.abs(dy) - Math.abs(prevDy);
        }

        if (gravityChange < 0.001) {
            gravityFailCount++;
            if (gravityFailCount > 5) {
                return CheckResult.FLAG;
            }
        } else if (gravityChange > 0.1) {
            gravityFailCount++;
            if (gravityFailCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            gravityFailCount = Math.max(0, gravityFailCount - 1);
        }
        return CheckResult.PASS;
    }
}
