package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ChestSpreadCheck extends AbstractCheck {

    private int chestSpreadCount;

    public ChestSpreadCheck(WatchcatConfig config) {
        super("ChestSpread", "world",
            config.getCheckWeight("world.chestspread", 0.35),
            config.isCheckEnabled("world.chestspread", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundContainerClickPacket click)) return CheckResult.PASS;
        int slot = click.slotNum();
        if (slot >= 0) {
            chestSpreadCount++;
            if (chestSpreadCount > 10) {
                return CheckResult.FLAG;
            }
        } else {
            chestSpreadCount = Math.max(0, chestSpreadCount - 1);
        }
        return CheckResult.PASS;
    }
}
