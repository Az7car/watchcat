package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class CollisionCheck extends AbstractCheck {

    private int collisionFailCount;
    private int insideBlockTicks;

    public CollisionCheck(WatchcatConfig config) {
        super("Collision", "movement",
            config.getCheckWeight("movement.collision", 0.65),
            config.isCheckEnabled("movement.collision", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        double dx = data.getPositionDelta().getX();
        double dz = data.getPositionDelta().getZ();
        double dh = Math.sqrt(dx * dx + dz * dz);

        if (dh > 0.1) {
            int x = (int) Math.floor(player.getLocation().getX() + dx);
            int z = (int) Math.floor(player.getLocation().getZ() + dz);
            int y = (int) Math.floor(player.getLocation().getY());

            org.bukkit.block.Block block = player.getWorld().getBlockAt(x, y, z);
            if (block.getType().isSolid() && !block.isLiquid()) {
                collisionFailCount++;
                if (collisionFailCount > 5) {
                    return CheckResult.FLAG;
                }
            } else {
                collisionFailCount = Math.max(0, collisionFailCount - 1);
            }
        }
        return CheckResult.PASS;
    }
}
