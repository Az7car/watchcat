package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoDragCheck extends AbstractCheck {

    private int noDragCount;
    private double lastHorizontalSpeed;

    public NoDragCheck(WatchcatConfig config) {
        super("NoDrag", "movement",
            config.getCheckWeight("movement.nodrag", 0.5),
            config.isCheckEnabled("movement.nodrag", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.isOnGround()) return CheckResult.PASS;
        double hv = data.getHorizontalVelocity();
        if (lastHorizontalSpeed > 0) {
            double decel = hv / lastHorizontalSpeed;
            if (decel > 0.99 && hv > 0.05) {
                noDragCount++;
                if (noDragCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                noDragCount = Math.max(0, noDragCount - 1);
            }
        }
        lastHorizontalSpeed = hv;
        return CheckResult.PASS;
    }
}
