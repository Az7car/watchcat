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
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HitboxCheck extends AbstractCheck {

    private final double maxExpansion;

    public HitboxCheck(WatchcatConfig config) {
        super("Hitbox", "combat",
            config.getCheckWeight("combat.hitbox"),
            config.isCheckEnabled("combat.hitbox"));
        this.maxExpansion = config.getCheckDouble("combat.hitbox", "max-expansion", 0.1);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        return process(player, data, packet, nmsPlayer) == CheckResult.FLAG ? CheckResult.CANCELLED : CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interactPacket)) return CheckResult.PASS;

        try {
            int entityId = interactPacket.getEntityId();
            net.minecraft.world.entity.Entity nmsTarget = nmsPlayer.serverLevel().getEntity(entityId);
            if (nmsTarget == null) return CheckResult.PASS;

            Vector eyePos = player.getEyeLocation().toVector();
            double[] dir = RotationUtils.toDirection(data.getYaw(), data.getPitch());
            Vector direction = new Vector(dir[0], dir[1], dir[2]);

            AABBUtils.AABB normalAABB = AABBUtils.fromNmsEntity(nmsTarget);
            AABBUtils.AABB expandedAABB = normalAABB.expand(0.3, 0.3, 0.3);

            double tNormal = AABBUtils.rayIntersectsAABB(eyePos, direction, normalAABB);
            double tExpanded = AABBUtils.rayIntersectsAABB(eyePos, direction, expandedAABB);

            if (tNormal < 0 && tExpanded >= 0) {
                return CheckResult.FLAG;
            }
        } catch (Exception ignored) {}

        return CheckResult.PASS;
    }
}
