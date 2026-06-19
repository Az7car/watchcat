package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ElevatorCheck extends AbstractCheck {

    private int elevatorCount;
    private double lastY;

    public ElevatorCheck(WatchcatConfig config) {
        super("Elevator", "movement",
            config.getCheckWeight("movement.elevator", 0.6),
            config.isCheckEnabled("movement.elevator", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (player.isFlying() || player.isInsideVehicle()) return CheckResult.PASS;
        double dy = data.getDeltaY();
        double horizontal = data.getHorizontalPositionDelta();
        if (dy > 1.5 && horizontal < 0.1) {
            boolean hasLadder = player.getLocation().getBlock().getType().name().contains("LADDER");
            boolean hasVine = player.getLocation().getBlock().getType().name().contains("VINE");
            boolean hasWater = player.isInWaterOrBubbleColumn();
            if (!hasLadder && !hasVine && !hasWater) {
                elevatorCount++;
                if (elevatorCount > 2) {
                    return CheckResult.FLAG;
                }
            }
        }
        if (elevatorCount > 0 && dy < 0.5) {
            elevatorCount = Math.max(0, elevatorCount - 1);
        }
        return CheckResult.PASS;
    }
}
