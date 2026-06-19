package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;

public class AutoBrewCheck extends AbstractCheck {

    private int autoBrewCount;

    public AutoBrewCheck(WatchcatConfig config) {
        super("AutoBrew", "world",
            config.getCheckWeight("world.autobrew", 0.35),
            config.isCheckEnabled("world.autobrew", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (inv instanceof BrewerInventory) {
            var items = inv.getContents();
            boolean allSlotsFilled = true;
            int filledCount = 0;
            for (var item : items) {
                if (item != null && !item.getType().isAir()) filledCount++;
            }
            if (filledCount >= 4) {
                autoBrewCount++;
                if (autoBrewCount > 5) {
                    return CheckResult.FLAG;
                }
            } else {
                autoBrewCount = Math.max(0, autoBrewCount - 1);
            }
        }
        return CheckResult.PASS;
    }
}
