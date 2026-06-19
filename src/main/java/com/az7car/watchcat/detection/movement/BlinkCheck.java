package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class BlinkCheck extends AbstractCheck {

    private final long maxPositionDelayMs;

    public BlinkCheck(WatchcatConfig config) {
        super("Blink", "movement",
            config.getCheckWeight("movement.blink"),
            config.isCheckEnabled("movement.blink"));
        this.maxPositionDelayMs = (long) config.getCheckDouble("movement.blink", "max-position-delay-ms", 500);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        long now = System.currentTimeMillis();
        long lastPacket = player.getTicksLived() > 0
            ? (System.nanoTime() - data.getLastPacketTime()) / 1_000_000
            : 0;

        if (lastPacket > 0 && lastPacket < 2000) {
            double delta = data.getPositionDelta();
            double expectedDelta = Math.min(1.0, lastPacket / 50.0 * 0.3);

            if (delta > expectedDelta * 5 && delta > 5) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
