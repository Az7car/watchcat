package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.Material;

public class StepHeightCheck extends AbstractCheck {

    private int stepHeightCount;

    public StepHeightCheck(WatchcatConfig config) {
        super("StepHeight", "movement",
            config.getCheckWeight("movement.stepheight", 0.55),
            config.isCheckEnabled("movement.stepheight", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        double dy = data.getDeltaY();
        double horizontal = data.getHorizontalPositionDelta();
        if (dy > 0.6 && dy < 1.2 && horizontal > 0.1) {
            var blockBelow = player.getLocation().clone().subtract(0, 1, 0).getBlock().getType();
            if (blockBelow != Material.LADDER && blockBelow.name().contains("VINE")) {
                stepHeightCount++;
                if (stepHeightCount > 3) {
                    return CheckResult.FLAG;
                }
            }
        } else {
            stepHeightCount = Math.max(0, stepHeightCount - 1);
        }
        return CheckResult.PASS;
    }
}
