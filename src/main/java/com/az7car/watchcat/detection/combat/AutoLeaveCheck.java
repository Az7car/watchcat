package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AutoLeaveCheck extends AbstractCheck {

    private int autoLeaveCount;
    private double lastHealth;

    public AutoLeaveCheck(WatchcatConfig config) {
        super("AutoLeave", "combat",
            config.getCheckWeight("combat.autoleave", 0.35),
            config.isCheckEnabled("combat.autoleave", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        double health = player.getHealth();
        if (lastHealth > 0 && health <= 4.0 && health < lastHealth - 2.0) {
            autoLeaveCount++;
            if (autoLeaveCount > 2) {
                return CheckResult.FLAG;
            }
        } else {
            autoLeaveCount = Math.max(0, autoLeaveCount - 1);
        }
        lastHealth = health;
        return CheckResult.PASS;
    }
}
