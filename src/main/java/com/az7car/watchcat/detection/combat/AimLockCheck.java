package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AimLockCheck extends AbstractCheck {

    private float lastDeltaYaw, lastDeltaPitch;
    private int lockedTicks;

    public AimLockCheck(WatchcatConfig config) {
        super("AimLock", "combat",
            config.getCheckWeight("combat.aimlock", 0.7),
            config.isCheckEnabled("combat.aimlock", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        float dyaw = data.getDeltaYaw();
        float dpitch = data.getDeltaPitch();

        if (Math.abs(dyaw) < 1.0f && Math.abs(dpitch) < 1.0f) {
            if (lastDeltaYaw != 0 && lastDeltaPitch != 0) {
                float yawRatio = dyaw / lastDeltaYaw;
                float pitchRatio = dpitch / lastDeltaPitch;
                if (Math.abs(yawRatio - pitchRatio) < 0.05f) {
                    lockedTicks++;
                    if (lockedTicks > 20) {
                        return CheckResult.FLAG;
                    }
                    return CheckResult.PASS;
                }
            }
        }
        lockedTicks = Math.max(0, lockedTicks - 1);
        lastDeltaYaw = dyaw;
        lastDeltaPitch = dpitch;
        return CheckResult.PASS;
    }
}
