package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class SneakCheck extends AbstractCheck {

    private int sprintSneakCount;

    public SneakCheck(WatchcatConfig config) {
        super("Sneak", "movement",
            config.getCheckWeight("movement.sneak", 0.6),
            config.isCheckEnabled("movement.sneak", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.isSneaking() && data.isSprinting()) {
            sprintSneakCount++;
            if (sprintSneakCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            sprintSneakCount = Math.max(0, sprintSneakCount - 1);
        }
        return CheckResult.PASS;
    }
}
