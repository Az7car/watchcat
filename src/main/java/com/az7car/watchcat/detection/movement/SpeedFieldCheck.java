package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class SpeedFieldCheck extends AbstractCheck {

    private int speedFieldCount;

    public SpeedFieldCheck(WatchcatConfig config) {
        super("SpeedField", "movement",
            config.getCheckWeight("movement.speedfield", 0.45),
            config.isCheckEnabled("movement.speedfield", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (player.isFlying() || player.isInsideVehicle()) return CheckResult.PASS;
        float walkSpeed = player.getWalkSpeed();
        if (walkSpeed > 0.2f) {
            speedFieldCount++;
            if (speedFieldCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            speedFieldCount = Math.max(0, speedFieldCount - 1);
        }
        return CheckResult.PASS;
    }
}
