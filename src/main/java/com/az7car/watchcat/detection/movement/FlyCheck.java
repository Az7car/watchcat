package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class FlyCheck extends AbstractCheck {

    private final double maxVerticalVelocity;

    public FlyCheck(WatchcatConfig config) {
        super("Fly", "movement",
            config.getCheckWeight("movement.fly"),
            config.isCheckEnabled("movement.fly"));
        this.maxVerticalVelocity = config.getCheckDouble("movement.fly", "max-vertical-velocity", 0.5);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;
        if (player.getAllowFlight() || player.isFlying()) return CheckResult.PASS;
        double deltaY = data.getDeltaY();
        if (Math.abs(deltaY) > maxVerticalVelocity * 3) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;

        if (player.getAllowFlight() || player.isFlying()) return CheckResult.PASS;

        double deltaY = data.getDeltaY();

        data.incAirTicks();

        if (Math.abs(deltaY) > maxVerticalVelocity) {
            return CheckResult.FLAG;
        }

        if (!data.isOnGround() && data.getAirTicks() > 5) {
            double predictedY = -0.08 * data.getAirTicks() * 0.02;
            if (deltaY > predictedY + 0.1) {
                return CheckResult.FLAG;
            }
        }

        double horizontal = data.getHorizontalPositionDelta();
        if (horizontal > 0.01 && !data.isOnGround() && data.getAirTicks() > 5) {
            double expectedFall = -0.08 * 0.98;
            if (deltaY > expectedFall + 0.05) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
