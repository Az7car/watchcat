package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class SpeedCheck extends AbstractCheck {

    private final double maxHorizontalSpeed;

    public SpeedCheck(WatchcatConfig config) {
        super("Speed", "movement",
            config.getCheckWeight("movement.speed"),
            config.isCheckEnabled("movement.speed"));
        this.maxHorizontalSpeed = config.getCheckDouble("movement.speed", "max-horizontal-speed", 0.65);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!isMovementPacket(packet)) return CheckResult.PASS;
        double deltaX = data.getDeltaX();
        double deltaZ = data.getDeltaZ();
        double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        if (horizontal > maxHorizontalSpeed * 3) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!isMovementPacket(packet)) return CheckResult.PASS;

        double deltaX = data.getDeltaX();
        double deltaZ = data.getDeltaZ();
        double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (horizontal > maxHorizontalSpeed) {
            if (data.getAirTicks() > 3) {
                double frictionFactor = 0.91;
                for (int i = 0; i < data.getAirTicks(); i++) {
                    horizontal *= frictionFactor;
                }
                if (horizontal > maxHorizontalSpeed * 1.5) {
                    return CheckResult.FLAG;
                }
            }
        }

        return CheckResult.PASS;
    }

    private boolean isMovementPacket(Packet<?> p) {
        return p instanceof ServerboundMovePlayerPacket
            || p instanceof ServerboundMovePlayerPacket.Pos
            || p instanceof ServerboundMovePlayerPacket.Rot
            || p instanceof ServerboundMovePlayerPacket.PosRot;
    }
}
