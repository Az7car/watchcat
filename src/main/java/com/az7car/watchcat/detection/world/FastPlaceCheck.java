package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class FastPlaceCheck extends AbstractCheck {

    private final double minPlaceIntervalVariance;

    public FastPlaceCheck(WatchcatConfig config) {
        super("FastPlace", "world",
            config.getCheckWeight("world.fastplace"),
            config.isCheckEnabled("world.fastplace"));
        this.minPlaceIntervalVariance = config.getCheckDouble("world.fastplace", "min-place-interval-variance", 0.005);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundUseItemOnPacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        long lastPlace = data.getLastBlockPlaceTime();
        data.recordBlockPlace();
        if (lastPlace <= 0) return CheckResult.PASS;
        long interval = now - lastPlace;
        if (interval <= 0) return CheckResult.PASS;
        if (interval < 20) return CheckResult.CANCELLED;
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundUseItemOnPacket)) return CheckResult.PASS;

        long now = System.currentTimeMillis();
        long lastPlace = data.getLastBlockPlaceTime();
        data.recordBlockPlace();

        if (lastPlace <= 0) return CheckResult.PASS;
        long interval = now - lastPlace;
        if (interval <= 0) return CheckResult.PASS;

        var clicks = data.getClickTimestamps();
        if (clicks.size() < 5) return CheckResult.PASS;

        double[] intervals = clicks.stream()
            .mapToLong(t -> now - t)
            .filter(t -> t > 0 && t < 1000)
            .mapToDouble(t -> (double) t)
            .toArray();

        if (intervals.length < 3) return CheckResult.PASS;

        double mean = 0;
        for (double i : intervals) mean += i;
        mean /= intervals.length;

        if (mean < 50) return CheckResult.FLAG;

        double variance = 0;
        for (double i : intervals) variance += Math.pow(i - mean, 2);
        variance /= intervals.length;

        if (variance < minPlaceIntervalVariance && mean < 200) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
