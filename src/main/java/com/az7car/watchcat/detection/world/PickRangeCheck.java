package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;

public class PickRangeCheck extends AbstractCheck {

    private int pickRangeCount;
    private static final double MAX_LEGIT_PICK_RANGE = 1.5;

    public PickRangeCheck(WatchcatConfig config) {
        super("PickRange", "world",
            config.getCheckWeight("world.pickrange", 0.4),
            config.isCheckEnabled("world.pickrange", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        var items = player.getNearbyEntities(10, 10, 10).stream()
            .filter(e -> e instanceof Item)
            .map(e -> (Item) e)
            .toList();
        for (var item : items) {
            double dist = player.getLocation().distance(item.getLocation());
            if (dist > MAX_LEGIT_PICK_RANGE && item.getPickupDelay() <= 0) {
                boolean isMovingToward = player.getLocation().getDirection()
                    .dot(item.getLocation().toVector().subtract(player.getLocation().toVector())) > 0;
                if (isMovingToward) {
                    pickRangeCount++;
                    if (pickRangeCount > 3) {
                        return CheckResult.FLAG;
                    }
                }
            }
        }
        if (pickRangeCount > 0) pickRangeCount = Math.max(0, pickRangeCount - 1);
        return CheckResult.PASS;
    }
}
