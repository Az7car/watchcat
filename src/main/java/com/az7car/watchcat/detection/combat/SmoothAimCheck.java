package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class SmoothAimCheck extends AbstractCheck {

    private int smoothAimCount;
    private float lastDeltaPitch;

    public SmoothAimCheck(WatchcatConfig config) {
        super("SmoothAim", "combat",
            config.getCheckWeight("combat.smoothaim", 0.5),
            config.isCheckEnabled("combat.smoothaim", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        float dp = data.getDeltaPitch();
        if (lastDeltaPitch != 0 && Math.abs(dp) > 0.5f) {
            float ratio = dp / lastDeltaPitch;
            if (ratio > 0.99 && ratio < 1.01 && Math.abs(dp) > 1.0f) {
                smoothAimCount++;
                if (smoothAimCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                smoothAimCount = Math.max(0, smoothAimCount - 1);
            }
        }
        lastDeltaPitch = dp;
        return CheckResult.PASS;
    }
}
