package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class GlideCheck extends AbstractCheck {

    private final double minDescentRate;

    public GlideCheck(WatchcatConfig config) {
        super("Glide", "movement",
            config.getCheckWeight("movement.glide"),
            config.isCheckEnabled("movement.glide"));
        this.minDescentRate = config.getCheckDouble("movement.glide", "min-descent-rate", 0.03);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.isOnGround() || player.isInWater()) return CheckResult.PASS;

        if (data.getAirTicks() > 5) {
            double deltaY = data.getDeltaY();
            if (deltaY < 0 && deltaY > -minDescentRate) {
                double horizontal = data.getHorizontalPositionDelta();
                if (horizontal > 0.1) {
                    return CheckResult.FLAG;
                }
            }
        }

        return CheckResult.PASS;
    }
}
