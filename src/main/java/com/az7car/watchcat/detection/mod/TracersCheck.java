package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class TracersCheck extends AbstractCheck {

    private int tracersCount;
    private float lastTargetYawDiff;

    public TracersCheck(WatchcatConfig config) {
        super("Tracers", "mod",
            config.getCheckWeight("mod.tracers", 0.45),
            config.isCheckEnabled("mod.tracers", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        var target = player.getTargetEntity(6);
        if (target == null) {
            tracersCount = Math.max(0, tracersCount - 1);
            return CheckResult.PASS;
        }
        var targetLoc = target.getLocation();
        var playerLoc = player.getLocation();
        double dx = targetLoc.getX() - playerLoc.getX();
        double dz = targetLoc.getZ() - playerLoc.getZ();
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float yawDiff = Math.abs(data.getYaw() - targetYaw);
        yawDiff = Math.min(yawDiff, 360 - yawDiff);

        if (lastTargetYawDiff != 0 && Math.abs(yawDiff - lastTargetYawDiff) < 0.5f && yawDiff < 10) {
            tracersCount++;
            if (tracersCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            tracersCount = Math.max(0, tracersCount - 1);
        }
        lastTargetYawDiff = yawDiff;
        return CheckResult.PASS;
    }
}
