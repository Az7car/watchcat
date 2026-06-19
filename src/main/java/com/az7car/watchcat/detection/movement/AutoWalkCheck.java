package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AutoWalkCheck extends AbstractCheck {

    private int autoWalkCount;
    private long lastMovePacket;

    public AutoWalkCheck(WatchcatConfig config) {
        super("AutoWalk", "movement",
            config.getCheckWeight("movement.autowalk", 0.35),
            config.isCheckEnabled("movement.autowalk", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        double hv = data.getHorizontalVelocity();
        float fwd = player.getWalkSpeed();
        boolean moving = hv > 0.05;
        boolean isAfk = data.getGroundTicks() > 60;
        if (moving && isAfk && fwd > 0.1f) {
            autoWalkCount++;
            if (autoWalkCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            autoWalkCount = Math.max(0, autoWalkCount - 1);
        }
        return CheckResult.PASS;
    }
}
