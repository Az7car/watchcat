package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import java.util.ArrayList;

public class TickShiftCheck extends AbstractCheck {

    private int tickShiftCount;
    private double lastHv;
    private long lastPacketNanos;

    public TickShiftCheck(WatchcatConfig config) {
        super("TickShift", "combat",
            config.getCheckWeight("combat.tickshift", 0.55),
            config.isCheckEnabled("combat.tickshift", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        double hv = data.getHorizontalVelocity();
        long now = System.nanoTime();
        long interval = lastPacketNanos == 0 ? 0 : (now - lastPacketNanos) / 1_000_000L;
        lastPacketNanos = now;

        if (interval > 0 && hv > 0.1 && lastHv > 0.1) {
            double speedRatio = hv / lastHv;
            boolean suddenSpeedDrop = speedRatio < 0.3 && lastHv > hv;
            boolean suddenSpeedSpike = speedRatio > 2.0 && hv > lastHv;

            if (suddenSpeedDrop || suddenSpeedSpike) {
                tickShiftCount++;
                if (tickShiftCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                tickShiftCount = Math.max(0, tickShiftCount - 1);
            }
        }
        lastHv = hv;
        return CheckResult.PASS;
    }
}
