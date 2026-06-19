package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class EnderPearlCheck extends AbstractCheck {

    private int enderPearlCount;
    private boolean hasPearlCooldown;

    public EnderPearlCheck(WatchcatConfig config) {
        super("EnderPearl", "movement",
            config.getCheckWeight("movement.enderpearl", 0.5),
            config.isCheckEnabled("movement.enderpearl", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        double hd = data.getHorizontalPositionDelta();
        boolean justThrewPearl = player.getCooldownPeriod() > 0;
        if (hd > 3.0 && !justThrewPearl) {
            enderPearlCount++;
            if (enderPearlCount > 2) {
                return CheckResult.FLAG;
            }
        } else {
            enderPearlCount = Math.max(0, enderPearlCount - 1);
        }
        return CheckResult.PASS;
    }
}
