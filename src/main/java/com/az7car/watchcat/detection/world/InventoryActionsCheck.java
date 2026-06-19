package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class InventoryActionsCheck extends AbstractCheck {

    private final double maxActionsPerSecond;
    private long lastActionTime;
    private int actionCount;
    private long windowStart;

    public InventoryActionsCheck(WatchcatConfig config) {
        super("InventoryActions", "world",
            config.getCheckWeight("world.inventoryactions", 0.5),
            config.isCheckEnabled("world.inventoryactions", true));
        this.maxActionsPerSecond = config.getCheckDouble("world.inventoryactions", "max-actions-per-second", 15);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundContainerClickPacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        if (now - windowStart > 1000) {
            actionCount = 0;
            windowStart = now;
        }
        actionCount++;
        if (actionCount > maxActionsPerSecond) {
            return CheckResult.FLAG;
        }
        lastActionTime = now;
        return CheckResult.PASS;
    }
}
