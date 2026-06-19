package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PistonPushCheck extends AbstractCheck {

    private int pistonPushCount;
    private int lastX, lastZ;

    public PistonPushCheck(WatchcatConfig config) {
        super("PistonPush", "world",
            config.getCheckWeight("world.pistonpush", 0.4),
            config.isCheckEnabled("world.pistonpush", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        int x = player.getLocation().getBlockX();
        int z = player.getLocation().getBlockZ();
        if (lastX != 0 || lastZ != 0) {
            int dx = Math.abs(x - lastX);
            int dz = Math.abs(z - lastZ);
            if (dx > 3 || dz > 3) {
                pistonPushCount++;
                if (pistonPushCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                pistonPushCount = Math.max(0, pistonPushCount - 1);
            }
        }
        lastX = x;
        lastZ = z;
        return CheckResult.PASS;
    }
}
