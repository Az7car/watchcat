package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SwimCheck extends AbstractCheck {

    private final double maxSwimSpeed;
    private int swimCount;

    public SwimCheck(WatchcatConfig config) {
        super("Swim", "movement",
            config.getCheckWeight("movement.swim", 0.5),
            config.isCheckEnabled("movement.swim", true));
        this.maxSwimSpeed = config.getCheckDouble("movement.swim", "max-swim-speed", 0.08);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        if (!player.getLocation().getBlock().getType().name().contains("WATER")) return CheckResult.PASS;
        if (move.isOnGround()) return CheckResult.PASS;

        double dx = data.getPositionDelta().getX();
        double dz = data.getPositionDelta().getZ();
        double dh = Math.sqrt(dx * dx + dz * dz);

        if (dh > maxSwimSpeed) {
            swimCount++;
            if (swimCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            swimCount = Math.max(0, swimCount - 1);
        }
        return CheckResult.PASS;
    }
}
