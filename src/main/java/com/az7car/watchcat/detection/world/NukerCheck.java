package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NukerCheck extends AbstractCheck {

    private final int maxBlocksPerSecond;

    public NukerCheck(WatchcatConfig config) {
        super("Nuker", "world",
            config.getCheckWeight("world.nuker"),
            config.isCheckEnabled("world.nuker"));
        this.maxBlocksPerSecond = (int) config.getCheckDouble("world.nuker", "max-blocks-per-second", 8);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket actionPacket)) return CheckResult.PASS;
        if (actionPacket.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
            || actionPacket.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            data.recordClick();
            var clicks = data.getClickTimestamps();
            if (clicks.size() < 3) return CheckResult.PASS;
            long now = System.currentTimeMillis();
            long oldest = clicks.peekFirst();
            if (oldest <= 0) return CheckResult.PASS;
            long window = now - oldest;
            if (window <= 0) return CheckResult.PASS;
            double bps = clicks.size() / (window / 1000.0);
            if (bps > 30) return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket actionPacket)) return CheckResult.PASS;

        if (actionPacket.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
            || actionPacket.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            data.recordClick();

            var clicks = data.getClickTimestamps();
            if (clicks.size() < 3) return CheckResult.PASS;

            long now = System.currentTimeMillis();
            long[] recent = clicks.stream()
                .mapToLong(t -> now - t)
                .filter(t -> t < 1000)
                .toArray();

            if (recent.length > maxBlocksPerSecond) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
