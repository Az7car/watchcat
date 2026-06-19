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

public class AscensionCheck extends AbstractCheck {

    private final double maxAscensionSpeed;
    private final double maxAscensionAccel;
    private double lastDy;
    private int ascensionCount;

    public AscensionCheck(WatchcatConfig config) {
        super("Ascension", "movement",
            config.getCheckWeight("movement.ascension", 0.65),
            config.isCheckEnabled("movement.ascension", true));
        this.maxAscensionSpeed = config.getCheckDouble("movement.ascension", "max-speed", 0.6);
        this.maxAscensionAccel = config.getCheckDouble("movement.ascension", "max-accel", 0.05);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        double dy = data.getPositionDelta().getY();
        if (dy <= 0) {
            lastDy = 0;
            return CheckResult.PASS;
        }
        if (player.isInsideVehicle() || player.isFlying()) return CheckResult.PASS;

        if (dy > maxAscensionSpeed) {
            ascensionCount++;
            if (ascensionCount > 2) {
                return CheckResult.FLAG;
            }
        } else if (lastDy > 0 && Math.abs(dy - lastDy) < maxAscensionAccel) {
            ascensionCount++;
            if (ascensionCount > 10) {
                return CheckResult.FLAG;
            }
        } else {
            ascensionCount = Math.max(0, ascensionCount - 1);
        }
        lastDy = dy;
        return CheckResult.PASS;
    }
}
