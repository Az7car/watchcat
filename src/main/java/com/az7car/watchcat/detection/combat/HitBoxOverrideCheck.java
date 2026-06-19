package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;

public class HitBoxOverrideCheck extends AbstractCheck {

    private int expansionCount;

    public HitBoxOverrideCheck(WatchcatConfig config) {
        super("HitBoxOverride", "combat",
            config.getCheckWeight("combat.hitboxoverride", 0.65),
            config.isCheckEnabled("combat.hitboxoverride", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        Entity target = null;
        try {
            int id = interact.getEntityId();
            for (Entity e : player.getWorld().getEntities()) {
                if (e.getEntityId() == id) { target = e; break; }
            }
        } catch (Exception e) {}
        if (target == null) return CheckResult.PASS;

        double distance = player.getEyeLocation().distance(target.getLocation());
        double hitboxWidth = 0.6;
        double maxReach = 3.2;

        org.bukkit.util.Vector toTarget = target.getLocation().toVector()
            .subtract(player.getEyeLocation().toVector());
        float yaw = data.getLastYaw();
        float pitch = data.getLastPitch();
        org.bukkit.util.Vector lookDir = new Vector(
            -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)),
            -Math.sin(Math.toRadians(pitch)),
            Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))
        ).normalize();

        double dot = lookDir.dot(toTarget.normalize());
        if (dot > 0.99 && distance > (maxReach + hitboxWidth)) {
            expansionCount++;
            if (expansionCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            expansionCount = Math.max(0, expansionCount - 1);
        }
        return CheckResult.PASS;
    }
}
