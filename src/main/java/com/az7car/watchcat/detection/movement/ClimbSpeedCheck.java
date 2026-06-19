package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ClimbSpeedCheck extends AbstractCheck {

    private int climbSpeedCount;
    private double lastY;

    public ClimbSpeedCheck(WatchcatConfig config) {
        super("ClimbSpeed", "movement",
            config.getCheckWeight("movement.climbspeed", 0.5),
            config.isCheckEnabled("movement.climbspeed", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!player.isClimbing()) {
            climbSpeedCount = Math.max(0, climbSpeedCount - 1);
            return CheckResult.PASS;
        }
        double dy = data.getDeltaY();
        if (dy > 0.2) {
            climbSpeedCount++;
            if (climbSpeedCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            climbSpeedCount = Math.max(0, climbSpeedCount - 1);
        }
        return CheckResult.PASS;
    }
}
