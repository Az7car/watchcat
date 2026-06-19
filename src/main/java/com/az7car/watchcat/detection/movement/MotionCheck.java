package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class MotionCheck extends AbstractCheck {

    private final double maxAcceleration;
    private double lastHorizontalSpeed;
    private int accelerationCount;

    public MotionCheck(WatchcatConfig config) {
        super("Motion", "movement",
            config.getCheckWeight("movement.motion", 0.7),
            config.isCheckEnabled("movement.motion", true));
        this.maxAcceleration = config.getCheckDouble("movement.motion", "max-acceleration", 0.05);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        double dx = data.getPositionDelta().getX();
        double dz = data.getPositionDelta().getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        if (lastHorizontalSpeed > 0 && !move.isOnGround()) {
            double acceleration = Math.abs(horizontal - lastHorizontalSpeed);
            if (acceleration > maxAcceleration) {
                accelerationCount++;
                if (accelerationCount > 5) {
                    return CheckResult.FLAG;
                }
            } else {
                accelerationCount = Math.max(0, accelerationCount - 1);
            }
        }
        lastHorizontalSpeed = horizontal;
        return CheckResult.PASS;
    }
}
