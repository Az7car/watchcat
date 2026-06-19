package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AimVelocityCheck extends AbstractCheck {

    private int aimVelocityCount;

    public AimVelocityCheck(WatchcatConfig config) {
        super("AimVelocity", "combat",
            config.getCheckWeight("combat.aimvelocity", 0.45),
            config.isCheckEnabled("combat.aimvelocity", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        float dy = data.getDeltaPitch();
        float ddy = Math.abs(dy - data.getLastDeltaPitch());
        if (ddy < 0.01f && Math.abs(dy) > 0.5f) {
            aimVelocityCount++;
            if (aimVelocityCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            aimVelocityCount = Math.max(0, aimVelocityCount - 1);
        }
        return CheckResult.PASS;
    }
}
