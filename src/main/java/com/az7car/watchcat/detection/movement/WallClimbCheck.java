package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class WallClimbCheck extends AbstractCheck {

    private int wallClimbCount;
    private boolean wasAgainstWall;

    public WallClimbCheck(WatchcatConfig config) {
        super("WallClimb", "movement",
            config.getCheckWeight("movement.wallclimb", 0.6),
            config.isCheckEnabled("movement.wallclimb", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        if (move.isOnGround() || player.isInsideVehicle() || player.isFlying()) return CheckResult.PASS;

        double dy = data.getPositionDelta().getY();
        if (dy <= 0) {
            wasAgainstWall = false;
            return CheckResult.PASS;
        }

        boolean againstWall = false;
        for (int dx = -1; dx <= 1; dx += 2) {
            Material block = player.getLocation().clone().add(dx, 0, 0).getBlock().getType();
            if (block.isSolid()) { againstWall = true; break; }
        }
        if (!againstWall) {
            for (int dz = -1; dz <= 1; dz += 2) {
                Material block = player.getLocation().clone().add(0, 0, dz).getBlock().getType();
                if (block.isSolid()) { againstWall = true; break; }
            }
        }

        if (againstWall && dy > 0.1) {
            wallClimbCount++;
            if (wallClimbCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            wallClimbCount = Math.max(0, wallClimbCount - 1);
        }
        return CheckResult.PASS;
    }
}
