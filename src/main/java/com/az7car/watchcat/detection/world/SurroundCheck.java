package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SurroundCheck extends AbstractCheck {

    private int surroundCount;
    private long lastPlaceTime;

    public SurroundCheck(WatchcatConfig config) {
        super("Surround", "world",
            config.getCheckWeight("world.surround", 0.65),
            config.isCheckEnabled("world.surround", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return CheckResult.PASS;

        int bx = action.getPos().getX();
        int by = action.getPos().getY();
        int bz = action.getPos().getZ();
        Location loc = player.getLocation();

        double distance = Math.sqrt(
            Math.pow(bx - loc.getBlockX(), 2) +
            Math.pow(by - loc.getBlockY(), 2) +
            Math.pow(bz - loc.getBlockZ(), 2)
        );

        if (distance <= 1.5) {
            long now = System.currentTimeMillis();
            if (lastPlaceTime > 0 && (now - lastPlaceTime) < 50) {
                surroundCount++;
                if (surroundCount > 10) {
                    return CheckResult.FLAG;
                }
            } else {
                surroundCount = Math.max(0, surroundCount - 1);
            }
            lastPlaceTime = now;
        }
        return CheckResult.PASS;
    }
}
