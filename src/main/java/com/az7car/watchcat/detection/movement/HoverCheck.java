package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class HoverCheck extends AbstractCheck {

    private int hoverCount;

    public HoverCheck(WatchcatConfig config) {
        super("Hover", "movement",
            config.getCheckWeight("movement.hover", 0.55),
            config.isCheckEnabled("movement.hover", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.isOnGround() || player.isFlying() || player.isInsideVehicle()) return CheckResult.PASS;
        if (player.isInWaterOrBubbleColumn() || player.isClimbing()) return CheckResult.PASS;
        double dy = data.getDeltaY();
        if (Math.abs(dy) < 0.001 && data.getAirTicks() > 10) {
            hoverCount++;
            if (hoverCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            hoverCount = Math.max(0, hoverCount - 1);
        }
        return CheckResult.PASS;
    }
}
