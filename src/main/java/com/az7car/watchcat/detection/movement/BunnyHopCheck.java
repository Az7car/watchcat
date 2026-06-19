package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class BunnyHopCheck extends AbstractCheck {

    private final double maxHopRatio;

    public BunnyHopCheck(WatchcatConfig config) {
        super("BunnyHop", "movement",
            config.getCheckWeight("movement.bunnyhop"),
            config.isCheckEnabled("movement.bunnyhop"));
        this.maxHopRatio = config.getCheckDouble("movement.bunnyhop", "max-hop-ratio", 1.5);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.isOnGround()) return CheckResult.PASS;

        if (data.getAirTicks() == 1 && data.wasOnGround()) {
            double horizontal = data.getHorizontalPositionDelta();
            double lastHorizontal = Math.sqrt(
                Math.pow(data.getLastX() - data.getX(), 2) +
                Math.pow(data.getLastZ() - data.getZ(), 2));

            if (lastHorizontal > 0) {
                double ratio = horizontal / lastHorizontal;
                if (ratio > maxHopRatio) {
                    return CheckResult.FLAG;
                }
            }
        }

        return CheckResult.PASS;
    }
}
