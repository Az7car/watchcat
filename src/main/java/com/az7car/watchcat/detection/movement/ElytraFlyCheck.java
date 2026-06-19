package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ElytraFlyCheck extends AbstractCheck {

    private final double maxElytraSpeed;

    public ElytraFlyCheck(WatchcatConfig config) {
        super("ElytraFly", "movement",
            config.getCheckWeight("movement.elytrafly"),
            config.isCheckEnabled("movement.elytrafly"));
        this.maxElytraSpeed = config.getCheckDouble("movement.elytrafly", "max-elytra-speed", 2.0);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!player.isGliding()) return CheckResult.PASS;

        double horizontal = data.getHorizontalPositionDelta();
        if (horizontal > maxElytraSpeed) {
            return CheckResult.FLAG;
        }

        double deltaY = data.getDeltaY();
        if (deltaY > 0 && horizontal > 0.5) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
