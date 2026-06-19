package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AutoShieldCheck extends AbstractCheck {

    private final double perfectBlockThreshold;
    private Long lastBlockTime;

    public AutoShieldCheck(WatchcatConfig config) {
        super("AutoShield", "combat",
            config.getCheckWeight("combat.autoshield"),
            config.isCheckEnabled("combat.autoshield"));
        this.perfectBlockThreshold = config.getCheckDouble("combat.autoshield", "perfect-block-threshold", 0.02);
        this.lastBlockTime = null;
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!player.isBlocking()) return CheckResult.PASS;
        if (data.getLastAttackTime() > 0) {
            long now = System.currentTimeMillis();
            if (lastBlockTime != null) {
                long blockInterval = now - lastBlockTime;
                if (blockInterval > 50 && blockInterval < 1000) {
                    double deviation = Math.abs(blockInterval - 50.0) / 50.0;
                    if (deviation < perfectBlockThreshold / 2) {
                        return CheckResult.CANCELLED;
                    }
                }
            }
            lastBlockTime = now;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!player.isBlocking()) return CheckResult.PASS;

        if (data.getLastAttackTime() > 0) {
            long now = System.currentTimeMillis();
            long sinceAttack = now - data.getLastAttackTime();

            if (lastBlockTime != null) {
                long blockInterval = now - lastBlockTime;
                if (blockInterval > 50 && blockInterval < 1000) {
                    double deviation = Math.abs(blockInterval - 50.0) / 50.0;
                    if (deviation < perfectBlockThreshold) {
                        return CheckResult.FLAG;
                    }
                }
            }
            lastBlockTime = now;
        }

        return CheckResult.PASS;
    }
}
