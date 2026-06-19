package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class YPortCheck extends AbstractCheck {

    private int yPortCount;
    private boolean wasPositive;

    public YPortCheck(WatchcatConfig config) {
        super("YPort", "movement",
            config.getCheckWeight("movement.yport", 0.6),
            config.isCheckEnabled("movement.yport", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        if (player.isInsideVehicle() || player.isFlying()) return CheckResult.PASS;

        double dy = data.getDeltaY();
        if (dy == 0) return CheckResult.PASS;

        boolean isPositive = dy > 0;
        if (wasPositive != isPositive && Math.abs(dy) > 0.1) {
            yPortCount++;
            if (yPortCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            yPortCount = Math.max(0, yPortCount - 1);
        }
        wasPositive = isPositive;
        return CheckResult.PASS;
    }
}
