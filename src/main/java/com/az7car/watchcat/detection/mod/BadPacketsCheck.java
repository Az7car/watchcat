package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class BadPacketsCheck extends AbstractCheck {

    public BadPacketsCheck(WatchcatConfig config) {
        super("BadPackets", "mod",
            config.getCheckWeight("mod.badpackets"),
            config.isCheckEnabled("mod.badpackets"));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundMovePlayerPacket movePacket) {
            float yaw = movePacket.getYaw(0);
            float pitch = movePacket.getPitch(0);
            if (Float.isNaN(yaw) || Float.isNaN(pitch)
                || Float.isInfinite(yaw) || Float.isInfinite(pitch)) {
                return CheckResult.CANCELLED;
            }
            if (pitch > 90.0f || pitch < -90.0f) {
                return CheckResult.CANCELLED;
            }
        }
        if (packet instanceof ServerboundInteractPacket interactPacket) {
            if (interactPacket.getEntityId() == player.getEntityId()) {
                return CheckResult.CANCELLED;
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundMovePlayerPacket movePacket) {
            float yaw = movePacket.getYaw(0);
            float pitch = movePacket.getPitch(0);

            if (Float.isNaN(yaw) || Float.isNaN(pitch)
                || Float.isInfinite(yaw) || Float.isInfinite(pitch)) {
                return CheckResult.FLAG;
            }

            if (pitch > 90.0f || pitch < -90.0f) {
                return CheckResult.FLAG;
            }

            if (movePacket instanceof ServerboundMovePlayerPacket.Pos posPacket) {
                double x = posPacket.getX(0);
                double y = posPacket.getY(0);
                double z = posPacket.getZ(0);

                if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)
                    || Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z)) {
                    return CheckResult.FLAG;
                }
            }
        }

        if (packet instanceof ServerboundInteractPacket interactPacket) {
            if (interactPacket.getEntityId() == player.getEntityId()) {
                return CheckResult.FLAG;
            }
        }

        if (packet instanceof ServerboundUseItemOnPacket placePacket) {
            if (placePacket.getBlockPos().getY() < -64 || placePacket.getBlockPos().getY() > 320) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
