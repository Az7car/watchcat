package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class FreecamCheck extends AbstractCheck {

    private int inconsistencyCount;

    public FreecamCheck(WatchcatConfig config) {
        super("Freecam", "mod",
            config.getCheckWeight("mod.freecam", 0.8),
            config.isCheckEnabled("mod.freecam", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;

        double dx = data.getPositionDelta().getX();
        double dz = data.getPositionDelta().getZ();
        double dh = Math.sqrt(dx * dx + dz * dz);

        float yaw = packet instanceof PosRot ? ((PosRot) packet).getYaw(0) :
                    packet instanceof Rot ? ((Rot) packet).getYaw(0) :
                    data.getLastYaw();

        float pitch = packet instanceof PosRot ? ((PosRot) packet).getPitch(0) :
                      packet instanceof Rot ? ((Rot) packet).getPitch(0) :
                      data.getLastPitch();

        double lookDirX = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        double lookDirZ = -Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        double lookHorizontal = Math.sqrt(lookDirX * lookDirX + lookDirZ * lookDirZ);

        if (dh > 0.01 && lookHorizontal > 0.01) {
            double dot = (dx * lookDirX + dz * lookDirZ) / (dh * lookHorizontal);
            if (dot < -0.5) {
                inconsistencyCount++;
                if (inconsistencyCount > 10) {
                    inconsistencyCount = 0;
                    return CheckResult.FLAG;
                }
            } else {
                inconsistencyCount = Math.max(0, inconsistencyCount - 1);
            }
        }

        if (packet instanceof Pos || packet instanceof PosRot) {
            double y = packet instanceof Pos ? ((Pos) packet).getY(0) : ((PosRot) packet).getY(0);
            if (player.isInsideVehicle()) return CheckResult.PASS;
            if (y < player.getWorld().getMinHeight() - 10 || y > player.getWorld().getMaxHeight() + 10) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
