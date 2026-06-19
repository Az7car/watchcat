package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ShieldBreakerCheck extends AbstractCheck {

    private final long perfectTimingThreshold;
    private int breakCount;

    public ShieldBreakerCheck(WatchcatConfig config) {
        super("ShieldBreaker", "combat",
            config.getCheckWeight("combat.shieldbreaker", 0.7),
            config.isCheckEnabled("combat.shieldbreaker", true));
        this.perfectTimingThreshold = (long) config.getCheckDouble("combat.shieldbreaker", "perfect-timing-threshold", 50);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        if (interact.getAction() != ServerboundInteractPacket.Action.ATTACK) return CheckResult.PASS;

        Material hand = player.getInventory().getItemInMainHand().getType();
        if (!hand.name().endsWith("_AXE")) return CheckResult.PASS;

        long now = System.currentTimeMillis();
        Long lastAttack = data.getClickTimestamps().peekLast();
        if (lastAttack != null && lastAttack > 0) {
            long interval = now - lastAttack;
            if (interval > 0 && interval < perfectTimingThreshold) {
                breakCount++;
                if (breakCount > 3) {
                    return CheckResult.CANCELLED;
                }
            } else {
                breakCount = 0;
            }
        }

        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        if (interact.getAction() != ServerboundInteractPacket.Action.ATTACK) return CheckResult.PASS;

        Material hand = player.getInventory().getItemInMainHand().getType();
        if (!hand.name().endsWith("_AXE")) return CheckResult.PASS;

        long now = System.currentTimeMillis();
        Long lastAttack = data.getClickTimestamps().peekLast();
        if (lastAttack != null && lastAttack > 0) {
            long interval = now - lastAttack;
            if (interval > 0 && interval < perfectTimingThreshold) {
                breakCount++;
                if (breakCount > 2) {
                    return CheckResult.FLAG;
                }
            } else {
                breakCount = 0;
            }
        }

        return CheckResult.PASS;
    }
}
