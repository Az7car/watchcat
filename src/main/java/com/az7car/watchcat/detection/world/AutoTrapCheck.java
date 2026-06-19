package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.Material;

public class AutoTrapCheck extends AbstractCheck {

    private int autoTrapCount;

    public AutoTrapCheck(WatchcatConfig config) {
        super("AutoTrap", "world",
            config.getCheckWeight("world.autotrap", 0.45),
            config.isCheckEnabled("world.autotrap", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundUseItemOnPacket place)) return CheckResult.PASS;
        var item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) return CheckResult.PASS;
        String name = item.getType().name();
        boolean isTrapBlock = name.contains("OBSIDIAN") || name.contains("COBBLESTONE") || name.contains("ANVIL");
        if (!isTrapBlock) return CheckResult.PASS;

        var nearby = player.getNearbyEntities(3, 3, 3);
        boolean nearEnemy = nearby.stream().anyMatch(e -> e instanceof org.bukkit.entity.LivingEntity
            && !e.getUniqueId().equals(player.getUniqueId()));
        if (nearEnemy) {
            autoTrapCount++;
            if (autoTrapCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            autoTrapCount = Math.max(0, autoTrapCount - 1);
        }
        return CheckResult.PASS;
    }
}
