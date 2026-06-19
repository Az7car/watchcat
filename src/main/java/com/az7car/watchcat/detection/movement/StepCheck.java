package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class StepCheck extends AbstractCheck {

    private final double maxStepHeight;

    public StepCheck(WatchcatConfig config) {
        super("Step", "movement",
            config.getCheckWeight("movement.step"),
            config.isCheckEnabled("movement.step"));
        this.maxStepHeight = config.getCheckDouble("movement.step", "max-step-height", 0.6);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        double deltaY = data.getDeltaY();
        if (deltaY > maxStepHeight * 2 && deltaY < 1.5) {
            double horizontal = data.getHorizontalPositionDelta();
            if (horizontal > 0.1) {
                return CheckResult.CANCELLED;
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        double deltaY = data.getDeltaY();

        if (deltaY > maxStepHeight && deltaY < 1.5) {
            double horizontal = data.getHorizontalPositionDelta();
            if (horizontal > 0.1) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
