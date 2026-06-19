package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AimbotCheck extends AbstractCheck {

    private float lastDeltaYaw, lastDeltaPitch;
    private int perfectAimCount;
    private int aimSmoothnessScore;

    public AimbotCheck(WatchcatConfig config) {
        super("Aimbot", "mod",
            config.getCheckWeight("mod.aimbot", 0.75),
            config.isCheckEnabled("mod.aimbot", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof PosRot)) return CheckResult.PASS;
        PosRot rot = (PosRot) packet;
        float yaw = 0, pitch = 0;
        try {
            yaw = (float) rot.getClass().getMethod("getYaw").invoke(rot);
            pitch = (float) rot.getClass().getMethod("getPitch").invoke(rot);
        } catch (Exception e) { return CheckResult.PASS; }

        float dyaw = yaw - data.getLastYaw();
        float dpitch = pitch - data.getLastPitch();

        if (Math.abs(dyaw) < 0.01f && Math.abs(dpitch) < 0.01f) {
            return CheckResult.PASS;
        }

        if (perfectAimCount > 0 && lastDeltaYaw > 0 && lastDeltaPitch > 0) {
            float yawRatio = dyaw / lastDeltaYaw;
            float pitchRatio = dpitch / lastDeltaPitch;

            if (yawRatio > 0 && pitchRatio > 0 && Math.abs(yawRatio - pitchRatio) < 0.01) {
                perfectAimCount++;
            } else {
                perfectAimCount = Math.max(0, perfectAimCount - 1);
            }
        }

        float yawDiff = Math.abs(dyaw);
        float pitchDiff = Math.abs(dpitch);
        if (yawDiff > 0 && pitchDiff > 0) {
            float smoothness = yawDiff / pitchDiff;
            if (smoothness > 0.5f && smoothness < 2.0f) {
                aimSmoothnessScore++;
            } else {
                aimSmoothnessScore = Math.max(0, aimSmoothnessScore - 1);
            }
        }

        if (perfectAimCount > 15 || aimSmoothnessScore > 30) {
            return CheckResult.FLAG;
        }

        lastDeltaYaw = dyaw;
        lastDeltaPitch = dpitch;
        return CheckResult.PASS;
    }
}
