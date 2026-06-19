package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class TimeSpoofCheck extends AbstractCheck {

    private int timeSpoofCount;

    public TimeSpoofCheck(WatchcatConfig config) {
        super("TimeSpoof", "mod",
            config.getCheckWeight("mod.timespoof", 0.4),
            config.isCheckEnabled("mod.timespoof", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        long worldTime = player.getWorld().getTime();
        long playerTime = player.getPlayerTime();
        if (playerTime != worldTime) {
            boolean isRelative = player.isPlayerTimeRelative();
            if (!isRelative) {
                timeSpoofCount++;
                if (timeSpoofCount > 3) {
                    return CheckResult.FLAG;
                }
            }
        }
        if (timeSpoofCount > 0) {
            timeSpoofCount = Math.max(0, timeSpoofCount - 1);
        }
        return CheckResult.PASS;
    }
}
