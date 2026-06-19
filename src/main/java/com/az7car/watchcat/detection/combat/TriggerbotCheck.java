package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class TriggerbotCheck extends AbstractCheck {

    private final double perfectTimingThreshold;

    public TriggerbotCheck(WatchcatConfig config) {
        super("Triggerbot", "combat",
            config.getCheckWeight("combat.triggerbot"),
            config.isCheckEnabled("combat.triggerbot"));
        this.perfectTimingThreshold = config.getCheckDouble("combat.triggerbot", "perfect-timing-threshold", 0.01);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        int recentClicks = (int) data.getClickTimestamps().stream()
            .filter(t -> System.currentTimeMillis() - t < 500)
            .count();
        if (recentClicks > 15) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        data.recordClick();
        var clicks = data.getClickTimestamps();
        if (clicks.size() < 10) return CheckResult.PASS;

        long[] intervals = new long[clicks.size() - 1];
        var iter = clicks.iterator();
        long prev = iter.next();
        int i = 0;
        while (iter.hasNext()) {
            long curr = iter.next();
            intervals[i++] = curr - prev;
            prev = curr;
        }

        if (i < 5) return CheckResult.PASS;

        double mean = 0;
        for (long in : intervals) mean += in;
        mean /= i;

        if (mean <= 0) return CheckResult.PASS;
        double variance = 0;
        for (long in : intervals) variance += Math.pow(in - mean, 2);
        variance /= i;
        double stdDev = Math.sqrt(variance);

        double cv = stdDev / mean; // coefficient of variation
        if (cv < perfectTimingThreshold && mean < 200) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
