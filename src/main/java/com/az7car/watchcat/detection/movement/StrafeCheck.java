package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.util.MathUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class StrafeCheck extends AbstractCheck {

    private final double maxDirectionChangeSpeed;

    public StrafeCheck(WatchcatConfig config) {
        super("Strafe", "movement",
            config.getCheckWeight("movement.strafe"),
            config.isCheckEnabled("movement.strafe"));
        this.maxDirectionChangeSpeed = config.getCheckDouble("movement.strafe", "max-direction-change-speed", 0.6);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.isOnGround() || data.getAirTicks() < 2) return CheckResult.PASS;

        double deltaX = data.getDeltaX();
        double deltaZ = data.getDeltaZ();
        double lastDeltaX = data.getX() - data.getLastX();
        double lastDeltaZ = data.getZ() - data.getLastZ();

        double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double lastSpeed = Math.sqrt(lastDeltaX * lastDeltaX + lastDeltaZ * lastDeltaZ);

        if (speed > maxDirectionChangeSpeed && lastSpeed > 0.01) {
            double ratio = speed / lastSpeed;
            if (ratio > 1.3 && data.getAirTicks() > 2) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
