package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class HighJumpCheck extends AbstractCheck {

    private final double maxJumpVelocity;

    public HighJumpCheck(WatchcatConfig config) {
        super("HighJump", "movement",
            config.getCheckWeight("movement.highjump"),
            config.isCheckEnabled("movement.highjump"));
        this.maxJumpVelocity = config.getCheckDouble("movement.highjump", "max-jump-velocity", 0.55);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.isOnGround()) return CheckResult.PASS;

        if (data.getAirTicks() == 1) {
            double deltaY = data.getDeltaY();
            if (deltaY > maxJumpVelocity) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
