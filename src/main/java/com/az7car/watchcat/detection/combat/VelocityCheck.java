package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class VelocityCheck extends AbstractCheck {

    private final double minExpectedRatio;

    public VelocityCheck(WatchcatConfig config) {
        super("Velocity", "combat",
            config.getCheckWeight("combat.velocity"),
            config.isCheckEnabled("combat.velocity"));
        this.minExpectedRatio = config.getCheckDouble("combat.velocity", "min-expected-ratio", 0.6);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof net.minecraft.network.protocol.game.ServerboundPlayerInputPacket)) {
            return CheckResult.PASS;
        }
        if (data.getAirTicks() > 2 && data.getDeltaY() > 0) {
            double expected = 0.42 * Math.pow(0.98, data.getAirTicks() - 1);
            double actual = data.getVelocityY();
            if (expected > 0.1 && actual < expected * 0.1) {
                return CheckResult.CANCELLED;
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof net.minecraft.network.protocol.game.ServerboundPlayerInputPacket)) {
            return CheckResult.PASS;
        }

        if (data.getDeltaY() < 0 && !data.isOnGround()) return CheckResult.PASS;

        if (data.getDeltaY() > 0 && data.getAirTicks() > 1) {
            double expectedVertical = 0.42 * Math.pow(0.98, data.getAirTicks() - 1);
            double actualVertical = data.getVelocityY();
            if (expectedVertical > 0.1) {
                double ratio = actualVertical / expectedVertical;
                if (ratio < minExpectedRatio) {
                    return CheckResult.FLAG;
                }
            }
        }

        return CheckResult.PASS;
    }
}
