package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.util.MathUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class TickTimerAnalyzer extends AbstractCheck {

    private final double maxPacketRate;
    private final double maxRateStdDev;

    public TickTimerAnalyzer(WatchcatConfig config) {
        super("Timer", "mod",
            config.getCheckWeight("mod.timer"),
            config.isCheckEnabled("mod.timer"));
        this.maxPacketRate = config.getCheckDouble("mod.timer", "max-packet-rate", 22.5);
        this.maxRateStdDev = config.getCheckDouble("mod.timer", "max-rate-std-dev", 1.5);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        var timestamps = data.getPacketTimestamps();
        if (timestamps.size() < 10) return CheckResult.PASS;

        long now = System.nanoTime();
        long[] intervals = timestamps.stream()
            .mapToLong(t -> (now - t) / 1_000_000L)
            .filter(t -> t > 0 && t < 5000)
            .toArray();

        if (intervals.length < 5) return CheckResult.PASS;

        double mean = 0;
        for (long i : intervals) mean += i;
        mean /= intervals.length;

        if (mean <= 0) return CheckResult.PASS;
        double rate = 1000.0 / mean;

        if (rate > maxPacketRate) {
            return CheckResult.FLAG;
        }

        double variance = 0;
        for (long i : intervals) variance += Math.pow(i - mean, 2);
        variance /= intervals.length;
        double stdDev = Math.sqrt(variance);

        if (rate > 20.0 && stdDev < maxRateStdDev) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
