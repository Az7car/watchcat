package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class AutoSmithingCheck extends AbstractCheck {

    private int autoSmithCount;

    public AutoSmithingCheck(WatchcatConfig config) {
        super("AutoSmithing", "world",
            config.getCheckWeight("world.autosmithing", 0.35),
            config.isCheckEnabled("world.autosmithing", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (inv == null) return CheckResult.PASS;
        String type = inv.getType().name();
        if (!type.contains("SMITHING")) return CheckResult.PASS;
        var items = inv.getContents();
        int filled = 0;
        for (var item : items) {
            if (item != null && !item.getType().isAir()) filled++;
        }
        if (filled >= 2) {
            autoSmithCount++;
            if (autoSmithCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            autoSmithCount = Math.max(0, autoSmithCount - 1);
        }
        return CheckResult.PASS;
    }
}
