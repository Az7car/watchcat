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

public class FastDoorCheck extends AbstractCheck {

    private int fastDoorCount;
    private long lastDoorBreak;

    public FastDoorCheck(WatchcatConfig config) {
        super("FastDoor", "world",
            config.getCheckWeight("world.fastdoor", 0.4),
            config.isCheckEnabled("world.fastdoor", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return CheckResult.PASS;

        int bx = action.getPos().getX();
        int by = action.getPos().getY();
        int bz = action.getPos().getZ();
        Material block = player.getWorld().getBlockAt(bx, by, bz).getType();
        if (!block.name().contains("DOOR") && !block.name().contains("GATE")) return CheckResult.PASS;

        long now = System.currentTimeMillis();
        if (lastDoorBreak > 0 && (now - lastDoorBreak) < 100) {
            fastDoorCount++;
            if (fastDoorCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            fastDoorCount = Math.max(0, fastDoorCount - 1);
        }
        lastDoorBreak = now;
        return CheckResult.PASS;
    }
}
