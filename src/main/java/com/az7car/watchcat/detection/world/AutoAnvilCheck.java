package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AutoAnvilCheck extends AbstractCheck {

    private int autoAnvilCount;

    public AutoAnvilCheck(WatchcatConfig config) {
        super("AutoAnvil", "world",
            config.getCheckWeight("world.autoanvil", 0.35),
            config.isCheckEnabled("world.autoanvil", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (!(inv instanceof AnvilInventory)) return CheckResult.PASS;
        ItemStack item0 = inv.getItem(0);
        ItemStack item1 = inv.getItem(1);
        if (item0 != null && !item0.getType().isAir() && item1 != null && !item1.getType().isAir()) {
            autoAnvilCount++;
            if (autoAnvilCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            autoAnvilCount = Math.max(0, autoAnvilCount - 1);
        }
        return CheckResult.PASS;
    }
}
