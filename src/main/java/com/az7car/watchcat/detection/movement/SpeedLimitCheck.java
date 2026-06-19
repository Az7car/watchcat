package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class SpeedLimitCheck extends AbstractCheck {

    private final double absoluteMaxSpeed;
    private int speedLimitCount;

    public SpeedLimitCheck(WatchcatConfig config) {
        super("SpeedLimit", "movement",
            config.getCheckWeight("movement.speedlimit", 0.7),
            config.isCheckEnabled("movement.speedlimit", true));
        this.absoluteMaxSpeed = config.getCheckDouble("movement.speedlimit", "absolute-max", 2.0);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;
        double dx = data.getDeltaX();
        double dz = data.getDeltaZ();
        double dh = Math.sqrt(dx * dx + dz * dz);
        if (dh > absoluteMaxSpeed + 2.0 && !player.isInsideVehicle()) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;
        if (player.isInsideVehicle() || player.isFlying()) return CheckResult.PASS;
        double dx = data.getDeltaX();
        double dz = data.getDeltaZ();
        double dh = Math.sqrt(dx * dx + dz * dz);
        if (dh > absoluteMaxSpeed) {
            speedLimitCount++;
            if (speedLimitCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            speedLimitCount = Math.max(0, speedLimitCount - 1);
        }
        return CheckResult.PASS;
    }
}
