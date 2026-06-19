package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoFallCheck extends AbstractCheck {

    private final double maxFallDistance;

    public NoFallCheck(WatchcatConfig config) {
        super("NoFall", "movement",
            config.getCheckWeight("movement.nofall"),
            config.isCheckEnabled("movement.nofall"));
        this.maxFallDistance = config.getCheckDouble("movement.nofall", "max-fall-distance", 3.0);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;
        double deltaY = data.getDeltaY();
        if (deltaY < -0.5 && data.isOnGround() && data.getAirTicks() > 3) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;

        double fallDist = data.getFallDistance();

        if (fallDist > maxFallDistance && data.isOnGround()) {
            return CheckResult.FLAG;
        }

        if (packet instanceof ServerboundMovePlayerPacket.Pos || packet instanceof ServerboundMovePlayerPacket.PosRot) {
            double deltaY = data.getDeltaY();
            if (deltaY < -0.5 && data.isOnGround()) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
