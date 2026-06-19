package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ClipCheck extends AbstractCheck {

    private int clipCount;
    private double lastY;

    public ClipCheck(WatchcatConfig config) {
        super("Clip", "movement",
            config.getCheckWeight("movement.clip", 0.7),
            config.isCheckEnabled("movement.clip", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        if (player.isInsideVehicle() || player.isFlying()) return CheckResult.PASS;

        double y = 0;
        if (move instanceof ServerboundMovePlayerPacket.Pos) y = ((ServerboundMovePlayerPacket.Pos) move).getY(0);
        else if (move instanceof ServerboundMovePlayerPacket.PosRot) y = ((ServerboundMovePlayerPacket.PosRot) move).getY(0);
        else return CheckResult.PASS;

        if (lastY != 0) {
            double dy = y - lastY;
            if (Math.abs(dy) > 3.0) {
                clipCount++;
                if (clipCount > 2) {
                    return CheckResult.FLAG;
                }
            } else {
                clipCount = Math.max(0, clipCount - 1);
            }
        }
        lastY = y;
        return CheckResult.PASS;
    }
}
