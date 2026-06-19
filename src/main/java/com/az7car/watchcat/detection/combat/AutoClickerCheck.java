package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AutoClickerCheck extends AbstractCheck {

    private final double maxCps;
    private final double cpsStdDevThreshold;

    public AutoClickerCheck(WatchcatConfig config) {
        super("AutoClicker", "combat",
            config.getCheckWeight("combat.autoclicker"),
            config.isCheckEnabled("combat.autoclicker"));
        this.maxCps = config.getCheckDouble("combat.autoclicker", "max-cps", 18);
        this.cpsStdDevThreshold = config.getCheckDouble("combat.autoclicker", "cps-std-dev-threshold", 0.5);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        data.recordClick();
        var clicks = data.getClickTimestamps();
        if (clicks.size() < 3) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        long oldest = clicks.peekFirst();
        if (oldest <= 0) return CheckResult.PASS;
        long window = now - oldest;
        if (window <= 0) return CheckResult.PASS;
        double cps = (clicks.size() / (window / 1000.0));
        if (cps > 30) return CheckResult.CANCELLED;
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;

        data.recordClick();
        var clicks = data.getClickTimestamps();
        if (clicks.size() < 5) return CheckResult.PASS;

        long now = System.currentTimeMillis();
        long[] intervals = clicks.stream()
            .mapToLong(t -> now - t)
            .filter(t -> t > 0 && t < 2000)
            .toArray();

        if (intervals.length < 3) return CheckResult.PASS;

        double mean = 0;
        for (long i : intervals) mean += i;
        mean /= intervals.length;

        if (mean <= 0) return CheckResult.PASS;
        double cps = 1000.0 / mean;

        if (cps > maxCps) {
            return CheckResult.FLAG;
        }

        double variance = 0;
        for (long i : intervals) variance += Math.pow(i - mean, 2);
        variance /= intervals.length;
        double stdDev = Math.sqrt(variance);

        if (stdDev < cpsStdDevThreshold && cps > 8) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
