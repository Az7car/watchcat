package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class JumpCheck extends AbstractCheck {

    private final double maxJumpVelocity;
    private int jumpCount;

    public JumpCheck(WatchcatConfig config) {
        super("Jump", "movement",
            config.getCheckWeight("movement.jumpcheck", 0.55),
            config.isCheckEnabled("movement.jumpcheck", true));
        this.maxJumpVelocity = config.getCheckDouble("movement.jumpcheck", "max-velocity", 0.42);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        double dy = data.getPositionDelta().getY();
        if (dy <= 0 || move.isOnGround()) return CheckResult.PASS;
        if (player.isInsideVehicle() || player.isFlying()) return CheckResult.PASS;

        if (data.getLastPositionDelta().getY() <= 0 && dy > 0) {
            if (dy > maxJumpVelocity) {
                jumpCount++;
                if (jumpCount > 2) {
                    return CheckResult.FLAG;
                }
            } else {
                jumpCount = Math.max(0, jumpCount - 1);
            }
        }
        return CheckResult.PASS;
    }
}
