package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Firework;

public class AntiFireworkCheck extends AbstractCheck {

    private int fireworkBypassCount;

    public AntiFireworkCheck(WatchcatConfig config) {
        super("AntiFirework", "mod",
            config.getCheckWeight("mod.antifirework", 0.5),
            config.isCheckEnabled("mod.antifirework", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        double dy = data.getPositionDelta().getY();
        if (dy > 0.5) {
            boolean nearFirework = player.getNearbyEntities(3, 3, 3).stream()
                .anyMatch(e -> e instanceof Firework);
            if (!nearFirework) {
                fireworkBypassCount++;
                if (fireworkBypassCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                fireworkBypassCount = Math.max(0, fireworkBypassCount - 1);
            }
        }
        return CheckResult.PASS;
    }
}
