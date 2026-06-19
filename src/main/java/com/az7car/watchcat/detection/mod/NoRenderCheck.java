package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoRenderCheck extends AbstractCheck {

    private final double minRenderDistance;
    private int noRenderCount;

    public NoRenderCheck(WatchcatConfig config) {
        super("NoRender", "mod",
            config.getCheckWeight("mod.norender", 0.5),
            config.isCheckEnabled("mod.norender", true));
        this.minRenderDistance = config.getCheckDouble("mod.norender", "min-distance", 0.1);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (player.getWorld().getPlayers().size() < 2) return CheckResult.PASS;

        double closest = Double.MAX_VALUE;
        for (org.bukkit.entity.Player other : player.getWorld().getPlayers()) {
            if (other.equals(player)) continue;
            double dist = player.getLocation().distance(other.getLocation());
            if (dist < closest) closest = dist;
        }

        if (closest < 5.0) {
            float yaw = data.getLastYaw();
            float pitch = data.getLastPitch();
            for (org.bukkit.entity.Player other : player.getWorld().getPlayers()) {
                if (other.equals(player)) continue;
                org.bukkit.util.Vector toTarget = other.getLocation().toVector()
                    .subtract(player.getEyeLocation().toVector()).normalize();
                double dx = toTarget.getX();
                double dz = toTarget.getZ();
                double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
                double angleDiff = Math.abs(yaw - targetYaw);
                if (angleDiff > 180) angleDiff = 360 - angleDiff;
                if (angleDiff < 30) {
                    return CheckResult.PASS;
                }
            }
            noRenderCount++;
            if (noRenderCount > 20) {
                return CheckResult.FLAG;
            }
        } else {
            noRenderCount = 0;
        }
        return CheckResult.PASS;
    }
}
