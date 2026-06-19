package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ScaffoldCheck extends AbstractCheck {

    private final double maxAngleDeviation;

    public ScaffoldCheck(WatchcatConfig config) {
        super("Scaffold", "world",
            config.getCheckWeight("world.scaffold"),
            config.isCheckEnabled("world.scaffold"));
        this.maxAngleDeviation = config.getCheckDouble("world.scaffold", "max-angle-deviation", 15.0);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundUseItemOnPacket placePacket)) return CheckResult.PASS;
        var block = player.getTargetBlockExact(5);
        if (block == null) return CheckResult.PASS;
        double dx = block.getX() - player.getLocation().getX();
        double dz = block.getZ() - player.getLocation().getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 4.5) return CheckResult.PASS;
        double dirLen = Math.sqrt(
            placePacket.getHitVector().x * placePacket.getHitVector().x
            + placePacket.getHitVector().y * placePacket.getHitVector().y
            + placePacket.getHitVector().z * placePacket.getHitVector().z);
        double lookupAngle = Math.abs(placePacket.getHitVector().y / dirLen);
        if (lookupAngle > 0.98) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundUseItemOnPacket placePacket)) return CheckResult.PASS;

        Vector eye = player.getEyeLocation().getDirection();
        double dirX = placePacket.getHitVector().x;
        double dirY = placePacket.getHitVector().y;
        double dirZ = placePacket.getHitVector().z;

        Vector hitVec = new Vector(dirX, dirY, dirZ).normalize();

        double angle = eye.angle(hitVec);
        if (angle < Math.toRadians(maxAngleDeviation)) {
            data.recordBlockPlace();
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
