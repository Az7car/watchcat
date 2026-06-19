package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;

public class BoatFlyCheck extends AbstractCheck {

    private final double maxBoatSpeed;

    public BoatFlyCheck(WatchcatConfig config) {
        super("BoatFly", "movement",
            config.getCheckWeight("movement.boatfly"),
            config.isCheckEnabled("movement.boatfly"));
        this.maxBoatSpeed = config.getCheckDouble("movement.boatfly", "max-boat-speed", 0.6);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!player.isInsideVehicle() || !(player.getVehicle() instanceof Boat)) return CheckResult.PASS;

        double horizontal = data.getHorizontalPositionDelta();
        if (horizontal > maxBoatSpeed) {
            return CheckResult.FLAG;
        }

        double deltaY = data.getDeltaY();
        if (Math.abs(deltaY) > 0.1 && !player.getVehicle().isOnGround()) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
