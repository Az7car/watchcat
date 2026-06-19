package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class AutoEnchantCheck extends AbstractCheck {

    private int autoEnchantCount;

    public AutoEnchantCheck(WatchcatConfig config) {
        super("AutoEnchant", "world",
            config.getCheckWeight("world.autoenchant", 0.35),
            config.isCheckEnabled("world.autoenchant", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        if (!(inv instanceof EnchantingInventory)) return CheckResult.PASS;
        ItemStack item = inv.getItem(0);
        if (item != null && !item.getType().isAir()) {
            int xpLevel = player.getLevel();
            boolean canEnchant = xpLevel >= 30;
            if (canEnchant) {
                autoEnchantCount++;
                if (autoEnchantCount > 5) {
                    return CheckResult.FLAG;
                }
            }
        } else {
            autoEnchantCount = Math.max(0, autoEnchantCount - 1);
        }
        return CheckResult.PASS;
    }
}
