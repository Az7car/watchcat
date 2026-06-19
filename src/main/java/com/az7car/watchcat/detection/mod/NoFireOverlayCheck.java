package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoFireOverlayCheck extends AbstractCheck {

    private int noFireOverlayCount;
    private long lastFireStart;

    public NoFireOverlayCheck(WatchcatConfig config) {
        super("NoFireOverlay", "mod",
            config.getCheckWeight("mod.nofireoverlay", 0.35),
            config.isCheckEnabled("mod.nofireoverlay", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        int fireTicks = player.getFireTicks();
        if (fireTicks > 0) {
            lastFireStart = System.currentTimeMillis();
        }
        if (lastFireStart != 0 && fireTicks == 0) {
            long elapsed = System.currentTimeMillis() - lastFireStart;
            if (elapsed < 1000) {
                boolean hasWater = player.isInWaterOrBubbleColumn();
                boolean hasRain = player.getWorld().hasStorm() && player.getWorld().getHighestBlockYAt(player.getLocation()) < player.getLocation().getY();
                if (!hasWater && !hasRain) {
                    noFireOverlayCount++;
                    if (noFireOverlayCount > 2) {
                        return CheckResult.FLAG;
                    }
                }
            }
        }
        if (noFireOverlayCount > 0 && fireTicks > 0) {
            noFireOverlayCount = Math.max(0, noFireOverlayCount - 1);
        }
        return CheckResult.PASS;
    }
}
