package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class JesusCheck extends AbstractCheck {

    private final double maxWaterSpeed;

    public JesusCheck(WatchcatConfig config) {
        super("Jesus", "movement",
            config.getCheckWeight("movement.jesus"),
            config.isCheckEnabled("movement.jesus"));
        this.maxWaterSpeed = config.getCheckDouble("movement.jesus", "max-water-speed", 0.12);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!player.isInWater() && !player.isInLava()) return CheckResult.PASS;

        double horizontal = data.getHorizontalPositionDelta();
        double deltaY = data.getDeltaY();

        if (horizontal > maxWaterSpeed) {
            return CheckResult.FLAG;
        }

        if (Math.abs(deltaY) < 0.001 && horizontal > 0.01) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
