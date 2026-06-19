package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.ExperienceOrb;

public class ExpPickupCheck extends AbstractCheck {

    private int expPickupCount;
    private static final double MAX_LEGIT_EXP_RANGE = 1.0;

    public ExpPickupCheck(WatchcatConfig config) {
        super("ExpPickup", "world",
            config.getCheckWeight("world.exppickup", 0.35),
            config.isCheckEnabled("world.exppickup", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        var xpOrbs = player.getNearbyEntities(10, 10, 10).stream()
            .filter(e -> e instanceof ExperienceOrb)
            .map(e -> (ExperienceOrb) e)
            .toList();
        for (var orb : xpOrbs) {
            double dist = player.getLocation().distance(orb.getLocation());
            if (dist > MAX_LEGIT_EXP_RANGE) {
                expPickupCount++;
                if (expPickupCount > 5) {
                    return CheckResult.FLAG;
                }
            }
        }
        if (expPickupCount > 0) expPickupCount = Math.max(0, expPickupCount - 1);
        return CheckResult.PASS;
    }
}
