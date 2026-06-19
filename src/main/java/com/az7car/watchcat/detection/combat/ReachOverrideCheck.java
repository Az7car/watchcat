package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ReachOverrideCheck extends AbstractCheck {

    private final double maxReach;
    private int reachOverrideCount;

    public ReachOverrideCheck(WatchcatConfig config) {
        super("ReachOverride", "combat",
            config.getCheckWeight("combat.reachoverride", 0.7),
            config.isCheckEnabled("combat.reachoverride", true));
        this.maxReach = config.getCheckDouble("combat.reachoverride", "max-reach", 4.0);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        float yaw = data.getLastYaw();
        float pitch = data.getLastPitch();
        Vector direction = new Vector(
            -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)),
            -Math.sin(Math.toRadians(pitch)),
            Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))
        ).normalize();

        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(6, 6, 6)) {
            if (entity.equals(player)) continue;
            Vector toTarget = entity.getLocation().toVector().subtract(player.getEyeLocation().toVector());
            double distance = toTarget.length();
            double dot = direction.dot(toTarget.normalize());
            if (dot > 0.99 && distance > maxReach) {
                reachOverrideCount++;
                if (reachOverrideCount > 3) {
                    return CheckResult.FLAG;
                }
                return CheckResult.PASS;
            }
        }
        reachOverrideCount = Math.max(0, reachOverrideCount - 1);
        return CheckResult.PASS;
    }
}
