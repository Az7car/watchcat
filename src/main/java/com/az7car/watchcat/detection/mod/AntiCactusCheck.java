package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.Material;

public class AntiCactusCheck extends AbstractCheck {

    private int antiCactusCount;
    private long lastCactusContact;

    public AntiCactusCheck(WatchcatConfig config) {
        super("AntiCactus", "mod",
            config.getCheckWeight("mod.anticactus", 0.4),
            config.isCheckEnabled("mod.anticactus", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        boolean nearCactus = player.getNearbyEntities(0.5, 0.5, 0.5).stream()
            .anyMatch(e -> e.getType().name().equals("CACTUS"));
        boolean nearCactusBlock = false;
        var loc = player.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (loc.clone().add(dx, dy, dz).getBlock().getType() == Material.CACTUS) {
                        nearCactusBlock = true;
                    }
                }
            }
        }
        if (nearCactusBlock && player.getNoDamageTicks() == 0 && player.getHealth() == player.getMaxHealth()) {
            antiCactusCount++;
            if (antiCactusCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            antiCactusCount = Math.max(0, antiCactusCount - 1);
        }
        return CheckResult.PASS;
    }
}
