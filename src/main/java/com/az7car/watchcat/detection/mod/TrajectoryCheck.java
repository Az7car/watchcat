package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class TrajectoryCheck extends AbstractCheck {

    private int trajectoryCount;
    private long lastLookChange;

    public TrajectoryCheck(WatchcatConfig config) {
        super("Trajectory", "mod",
            config.getCheckWeight("mod.trajectory", 0.5),
            config.isCheckEnabled("mod.trajectory", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        if (!player.getInventory().getItemInMainHand().getType().name().contains("BOW")) {
            return CheckResult.PASS;
        }

        float yaw = data.getLastYaw();
        float pitch = data.getLastPitch();
        long now = System.currentTimeMillis();

        if (data.getLastYaw() != yaw || data.getLastPitch() != pitch) {
            lastLookChange = now;
        }

        if (player.isHandRaised() && (now - lastLookChange) > 2000) {
            trajectoryCount++;
            if (trajectoryCount > 10) {
                return CheckResult.FLAG;
            }
        } else {
            trajectoryCount = Math.max(0, trajectoryCount - 1);
        }
        return CheckResult.PASS;
    }
}
