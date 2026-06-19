package com.az7car.watchcat.core.pipeline;

import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.detection.base.ViolationBuffer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public abstract class AbstractCheck {

    protected final String name;
    protected final String category;
    protected final double weight;
    protected final boolean enabled;

    public AbstractCheck(String name, String category, double weight, boolean enabled) {
        this.name = name;
        this.category = category;
        this.weight = weight;
        this.enabled = enabled;
    }

    public abstract CheckResult process(Player player, PlayerData data,
                                         Packet<?> packet, ServerPlayer nmsPlayer);

    public CheckResult processSync(Player player, PlayerData data,
                                    Packet<?> packet, ServerPlayer nmsPlayer) {
        return CheckResult.PASS;
    }

    public final CheckResult runWithProfiling(Player player, PlayerData data,
                                               Packet<?> packet, ServerPlayer nmsPlayer) {
        long start = System.nanoTime();
        try {
            return process(player, data, packet, nmsPlayer);
        } finally {
            CheckProfiler.record(name, System.nanoTime() - start);
        }
    }

    public final CheckResult runSyncWithProfiling(Player player, PlayerData data,
                                                   Packet<?> packet, ServerPlayer nmsPlayer) {
        long start = System.nanoTime();
        try {
            return processSync(player, data, packet, nmsPlayer);
        } finally {
            CheckProfiler.record(name + "_sync", System.nanoTime() - start);
        }
    }

    public void onTick(PlayerData data) {}

    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getWeight() { return weight; }
    public boolean isEnabled() { return enabled; }

    protected double normalizeViolations(int violations, int max) {
        return Math.min(1.0, (double) violations / max);
    }
}
