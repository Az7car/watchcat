package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class VehicleSpeedCheck extends AbstractCheck {

    private final double maxVehicleSpeed;
    private int vehicleCount;

    public VehicleSpeedCheck(WatchcatConfig config) {
        super("VehicleSpeed", "movement",
            config.getCheckWeight("movement.vehiclespeed", 0.5),
            config.isCheckEnabled("movement.vehiclespeed", true));
        this.maxVehicleSpeed = config.getCheckDouble("movement.vehiclespeed", "max-speed", 1.5);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        if (!player.isInsideVehicle()) return CheckResult.PASS;

        double dx = data.getPositionDelta().getX();
        double dz = data.getPositionDelta().getZ();
        double dh = Math.sqrt(dx * dx + dz * dz);

        if (dh > maxVehicleSpeed) {
            vehicleCount++;
            if (vehicleCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            vehicleCount = Math.max(0, vehicleCount - 1);
        }
        return CheckResult.PASS;
    }
}
