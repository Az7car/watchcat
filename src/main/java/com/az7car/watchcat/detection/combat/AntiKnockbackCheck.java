package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AntiKnockbackCheck extends AbstractCheck {

    private final double minVerticalVelocity;

    public AntiKnockbackCheck(WatchcatConfig config) {
        super("AntiKnockback", "combat",
            config.getCheckWeight("combat.antiknockback"),
            config.isCheckEnabled("combat.antiknockback"));
        this.minVerticalVelocity = config.getCheckDouble("combat.antiknockback", "min-vertical-velocity", 0.2);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.getAirTicks() > 3 && data.getVelocityY() >= 0) {
            double dx = data.getDeltaX();
            double dz = data.getDeltaZ();
            double horizSpeed = Math.sqrt(dx * dx + dz * dz);
            if (horizSpeed < 0.001) {
                return CheckResult.CANCELLED;
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.getAirTicks() > 2 && data.getVelocityY() >= 0) {
            double dx = data.getDeltaX();
            double dz = data.getDeltaZ();
            double horizSpeed = Math.sqrt(dx * dx + dz * dz);

            if (horizSpeed < 0.01 && data.getDeltaY() > -0.1) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
