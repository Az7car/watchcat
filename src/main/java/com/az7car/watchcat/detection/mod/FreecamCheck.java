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

        double dx = data.getDeltaX();
        double dz = data.getDeltaZ();
        double dh = Math.sqrt(dx * dx + dz * dz);

        float yaw = data.getLastYaw();
        float pitch = data.getLastPitch();
        try {
            if (packet instanceof PosRot) {
                yaw = (float) ((PosRot) packet).getClass().getMethod("getYaw").invoke(packet);
                pitch = (float) ((PosRot) packet).getClass().getMethod("getPitch").invoke(packet);
            } else if (packet instanceof Rot) {
                yaw = (float) ((Rot) packet).getClass().getMethod("getYaw").invoke(packet);
                pitch = (float) ((Rot) packet).getClass().getMethod("getPitch").invoke(packet);
            }
        } catch (Exception ignored) {}

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
            double y = 0;
            try {
                y = (double) packet.getClass().getMethod("getY").invoke(packet);
            } catch (Exception e) { return CheckResult.PASS; }
            if (player.isInsideVehicle()) return CheckResult.PASS;
            if (y < player.getWorld().getMinHeight() - 10 || y > player.getWorld().getMaxHeight() + 10) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
