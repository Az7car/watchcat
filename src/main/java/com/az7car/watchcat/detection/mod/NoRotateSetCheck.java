package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoRotateSetCheck extends AbstractCheck {

    private float lastServerYaw;
    private float lastServerPitch;
    private int mismatchCount;

    public NoRotateSetCheck(WatchcatConfig config) {
        super("NoRotateSet", "mod",
            config.getCheckWeight("mod.norotateset", 0.7),
            config.isCheckEnabled("mod.norotateset", true));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ClientboundRotateHeadPacket headPacket) {
            lastServerYaw = headPacket.getYaw();
        }
        if (packet instanceof ClientboundSetEntityDataPacket) {
            return CheckResult.PASS;
        }
        if (packet instanceof net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot
            || packet instanceof net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot) {
            float clientYaw = data.getYaw();
            if (lastServerYaw != 0 && Math.abs(clientYaw - lastServerYaw) > 90) {
                mismatchCount++;
                if (mismatchCount > 3) {
                    return CheckResult.CANCELLED;
                }
            } else {
                mismatchCount = 0;
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ClientboundRotateHeadPacket headPacket) {
            lastServerYaw = headPacket.getYaw();
        }
        if (packet instanceof net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot
            || packet instanceof net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot) {
            float clientYaw = data.getYaw();
            if (lastServerYaw != 0 && Math.abs(clientYaw - lastServerYaw) > 90) {
                mismatchCount++;
                if (mismatchCount > 5) {
                    return CheckResult.FLAG;
                }
            } else {
                mismatchCount = 0;
            }
        }
        return CheckResult.PASS;
    }
}
