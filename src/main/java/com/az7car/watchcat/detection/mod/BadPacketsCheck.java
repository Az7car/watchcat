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

    private static float extractYaw(ServerboundMovePlayerPacket pkt) {
        try {
            if (pkt instanceof ServerboundMovePlayerPacket.Rot) return (float) pkt.getClass().getMethod("getYaw").invoke(pkt);
            if (pkt instanceof ServerboundMovePlayerPacket.PosRot) return (float) pkt.getClass().getMethod("getYaw").invoke(pkt);
        } catch (Exception ignored) {}
        return 0;
    }

    private static float extractPitch(ServerboundMovePlayerPacket pkt) {
        try {
            if (pkt instanceof ServerboundMovePlayerPacket.Rot) return (float) pkt.getClass().getMethod("getPitch").invoke(pkt);
            if (pkt instanceof ServerboundMovePlayerPacket.PosRot) return (float) pkt.getClass().getMethod("getPitch").invoke(pkt);
        } catch (Exception ignored) {}
        return 0;
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundMovePlayerPacket movePacket) {
            float yaw = extractYaw(movePacket);
            float pitch = extractPitch(movePacket);
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
            float yaw = extractYaw(movePacket);
            float pitch = extractPitch(movePacket);

            if (Float.isNaN(yaw) || Float.isNaN(pitch)
                || Float.isInfinite(yaw) || Float.isInfinite(pitch)) {
                return CheckResult.FLAG;
            }

            if (pitch > 90.0f || pitch < -90.0f) {
                return CheckResult.FLAG;
            }

            if (movePacket instanceof ServerboundMovePlayerPacket.Pos posPacket) {
                double x, y, z;
                try {
                    x = (double) posPacket.getClass().getMethod("getX").invoke(posPacket);
                    y = (double) posPacket.getClass().getMethod("getY").invoke(posPacket);
                    z = (double) posPacket.getClass().getMethod("getZ").invoke(posPacket);
                } catch (Exception e) { return CheckResult.PASS; }

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
            try {
                Object hitResult = placePacket.getClass().getMethod("getHitResult").invoke(placePacket);
                Object blockPos = hitResult.getClass().getMethod("getBlockPos").invoke(hitResult);
                int y = (int) blockPos.getClass().getMethod("getY").invoke(blockPos);
                if (y < -64 || y > 320) return CheckResult.FLAG;
            } catch (Exception ignored) {}
        }

        return CheckResult.PASS;
    }
}
