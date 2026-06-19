package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.util.AABBUtils;
import com.az7car.watchcat.util.RotationUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ReachCheck extends AbstractCheck {

    private final double maxReach;
    private final double buffer;

    public ReachCheck(WatchcatConfig config) {
        super("Reach", "combat",
            config.getCheckWeight("combat.reach"),
            config.isCheckEnabled("combat.reach"));
        this.maxReach = config.getCheckDouble("combat.reach", "max-reach", 3.2);
        this.buffer = config.getCheckDouble("combat.reach", "buffer", 0.15);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interactPacket)) return CheckResult.PASS;
        try {
            int entityId = interactPacket.getEntityId();
            net.minecraft.world.entity.Entity nmsTarget = nmsPlayer.level().getEntity(entityId);
            if (nmsTarget == null) return CheckResult.PASS;
            Vector eyePos = player.getEyeLocation().toVector();
            double[] dir = RotationUtils.toDirection(data.getYaw(), data.getPitch());
            Vector direction = new Vector(dir[0], dir[1], dir[2]);
            AABBUtils.AABB targetAABB = AABBUtils.fromNmsEntity(nmsTarget);
            double t = AABBUtils.rayIntersectsAABB(eyePos, direction, targetAABB);
            if (t < 0) return CheckResult.PASS;
            double distance = eyePos.distance(direction.clone().multiply(t).add(eyePos));
            if (distance > maxReach + buffer + 0.5) {
                return CheckResult.CANCELLED;
            }
        } catch (Exception ignored) {}
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interactPacket)) return CheckResult.PASS;

        try {
            int entityId = interactPacket.getEntityId();
            net.minecraft.world.entity.Entity nmsTarget = nmsPlayer.level().getEntity(entityId);
            if (nmsTarget == null) return CheckResult.PASS;

            Vector eyePos = player.getEyeLocation().toVector();
            double[] dir = RotationUtils.toDirection(data.getYaw(), data.getPitch());
            Vector direction = new Vector(dir[0], dir[1], dir[2]);
            AABBUtils.AABB targetAABB = AABBUtils.fromNmsEntity(nmsTarget);

            double t = AABBUtils.rayIntersectsAABB(eyePos, direction, targetAABB);
            if (t < 0) return CheckResult.PASS;

            double distance = eyePos.distance(direction.clone().multiply(t).add(eyePos));
            data.recordReach(distance);

            if (distance > maxReach + buffer) {
                return CheckResult.FLAG;
            }
        } catch (Exception ignored) {}

        return CheckResult.PASS;
    }
}
