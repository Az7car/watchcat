package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoJumpDelayCheck extends AbstractCheck {

    private int noJumpDelayCount;
    private long lastJumpTime;

    public NoJumpDelayCheck(WatchcatConfig config) {
        super("NoJumpDelay", "movement",
            config.getCheckWeight("movement.nojumpdelay", 0.45),
            config.isCheckEnabled("movement.nojumpdelay", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        boolean jumping = !data.isOnGround() && data.wasOnGround();
        if (jumping) {
            long now = System.currentTimeMillis();
            if (lastJumpTime != 0 && now - lastJumpTime < 250) {
                noJumpDelayCount++;
                if (noJumpDelayCount > 3) {
                    return CheckResult.FLAG;
                }
            }
            lastJumpTime = now;
        } else {
            noJumpDelayCount = Math.max(0, noJumpDelayCount - 1);
        }
        return CheckResult.PASS;
    }
}
