package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ClickTPCheck extends AbstractCheck {

    private int clickTPCount;
    private double lastX, lastZ;
    private long lastTeleportTime;

    public ClickTPCheck(WatchcatConfig config) {
        super("ClickTP", "mod",
            config.getCheckWeight("mod.clicktp", 0.7),
            config.isCheckEnabled("mod.clicktp", true));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        double x = 0, z = 0;
        try {
            if (move instanceof ServerboundMovePlayerPacket.Pos) {
                x = (double) ((ServerboundMovePlayerPacket.Pos) move).getClass().getMethod("getX").invoke(move);
                z = (double) ((ServerboundMovePlayerPacket.Pos) move).getClass().getMethod("getZ").invoke(move);
            } else if (move instanceof ServerboundMovePlayerPacket.PosRot) {
                x = (double) ((ServerboundMovePlayerPacket.PosRot) move).getClass().getMethod("getX").invoke(move);
                z = (double) ((ServerboundMovePlayerPacket.PosRot) move).getClass().getMethod("getZ").invoke(move);
            }
        } catch (Exception e) { return CheckResult.PASS; }
        if (x == 0 && z == 0) return CheckResult.PASS;

        if (data.getDeltaX() == 0 && data.getDeltaZ() == 0) {
            return CheckResult.PASS;
        }

        if (lastX != 0 || lastZ != 0) {
            double dx = Math.abs(x - lastX);
            double dz = Math.abs(z - lastZ);
            double dh = Math.sqrt(dx * dx + dz * dz);
            long now = System.currentTimeMillis();

            if (dh > 10 && (now - lastTeleportTime) > 500) {
                clickTPCount++;
                if (clickTPCount > 2) {
                    return CheckResult.CANCELLED;
                }
                lastTeleportTime = now;
            }
        }
        lastX = x;
        lastZ = z;
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        double x = 0, z = 0;
        try {
            if (move instanceof ServerboundMovePlayerPacket.Pos) {
                x = (double) ((ServerboundMovePlayerPacket.Pos) move).getClass().getMethod("getX").invoke(move);
                z = (double) ((ServerboundMovePlayerPacket.Pos) move).getClass().getMethod("getZ").invoke(move);
            } else if (move instanceof ServerboundMovePlayerPacket.PosRot) {
                x = (double) ((ServerboundMovePlayerPacket.PosRot) move).getClass().getMethod("getX").invoke(move);
                z = (double) ((ServerboundMovePlayerPacket.PosRot) move).getClass().getMethod("getZ").invoke(move);
            }
        } catch (Exception e) { return CheckResult.PASS; }
        if (x == 0 && z == 0) return CheckResult.PASS;

        if (lastX != 0 || lastZ != 0) {
            double dx = Math.abs(x - lastX);
            double dz = Math.abs(z - lastZ);
            double dh = Math.sqrt(dx * dx + dz * dz);
            long now = System.currentTimeMillis();

            if (dh > 5 && (now - lastTeleportTime) > 1000) {
                clickTPCount++;
                if (clickTPCount > 3) {
                    return CheckResult.FLAG;
                }
                lastTeleportTime = now;
            }
        }
        lastX = x;
        lastZ = z;
        return CheckResult.PASS;
    }
}
