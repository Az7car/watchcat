package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ScreenshareCheck extends AbstractCheck {

    private int suspiciousBehavior;
    private long[] clickTimestamps = new long[100];
    private int clickIndex;

    public ScreenshareCheck(WatchcatConfig config) {
        super("Screenshare", "mod",
            config.getCheckWeight("mod.screenshare", 0.4),
            config.isCheckEnabled("mod.screenshare", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        float dyaw = data.getDeltaYaw();
        float dpitch = data.getDeltaPitch();

        int clicks = 0;
        long now = System.currentTimeMillis();
        for (long t : clickTimestamps) {
            if (t > 0 && (now - t) < 1000) clicks++;
        }
        double averageCPS = clicks;

        if (Math.abs(dyaw) < 0.001f && Math.abs(dpitch) < 0.001f && averageCPS > 12) {
            suspiciousBehavior++;
            if (suspiciousBehavior > 10) {
                return CheckResult.FLAG;
            }
        } else {
            suspiciousBehavior = Math.max(0, suspiciousBehavior - 1);
        }
        return CheckResult.PASS;
    }

    public void recordClick() {
        clickTimestamps[clickIndex % clickTimestamps.length] = System.currentTimeMillis();
        clickIndex++;
    }
}
