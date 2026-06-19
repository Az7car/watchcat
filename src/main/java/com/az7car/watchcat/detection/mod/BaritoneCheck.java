package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class BaritoneCheck extends AbstractCheck {

    private int pathingCount;
    private double lastTargetX, lastTargetZ;
    private int pathConsistency;

    public BaritoneCheck(WatchcatConfig config) {
        super("Baritone", "mod",
            config.getCheckWeight("mod.baritone", 0.65),
            config.isCheckEnabled("mod.baritone", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        double dx = data.getPositionDelta().getX();
        double dz = data.getPositionDelta().getZ();
        double dh = Math.sqrt(dx * dx + dz * dz);
        double dy = data.getPositionDelta().getY();

        if (dh > 0.1) {
            double angle = Math.toDegrees(Math.atan2(dz, dx));
            if (lastTargetX != 0 || lastTargetZ != 0) {
                double expectedAngle = Math.toDegrees(Math.atan2(
                    player.getLocation().getZ() - lastTargetZ,
                    player.getLocation().getX() - lastTargetX));
                double angleDiff = Math.abs(angle - expectedAngle);
                if (angleDiff < 5.0) {
                    pathConsistency++;
                } else {
                    pathConsistency = Math.max(0, pathConsistency - 1);
                }
            }

            if (dh > 0.3 && dy == 0 && pathConsistency > 20) {
                pathingCount++;
                if (pathingCount > 5) {
                    return CheckResult.FLAG;
                }
            } else {
                pathingCount = Math.max(0, pathingCount - 1);
            }
        }
        lastTargetX = player.getLocation().getX();
        lastTargetZ = player.getLocation().getZ();
        return CheckResult.PASS;
    }
}
