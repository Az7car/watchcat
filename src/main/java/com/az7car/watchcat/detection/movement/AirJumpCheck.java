package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AirJumpCheck extends AbstractCheck {

    private int airJumpCount;
    private boolean wasInAir;
    private int airTicks;

    public AirJumpCheck(WatchcatConfig config) {
        super("AirJump", "movement",
            config.getCheckWeight("movement.airjump", 0.6),
            config.isCheckEnabled("movement.airjump", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        boolean onGround = move.isOnGround();
        double dy = data.getDeltaY();

        if (!onGround) {
            airTicks++;
            wasInAir = true;
        } else {
            if (wasInAir && airTicks > 5 && dy >= 0.42) {
                airJumpCount++;
                if (airJumpCount > 2) {
                    return CheckResult.FLAG;
                }
            } else {
                airJumpCount = Math.max(0, airJumpCount - 1);
            }
            airTicks = 0;
            wasInAir = false;
        }
        return CheckResult.PASS;
    }
}
