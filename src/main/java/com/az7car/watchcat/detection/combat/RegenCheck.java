package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class RegenCheck extends AbstractCheck {

    private long lastHealTime;
    private int healCount;
    private double lastHealth;

    public RegenCheck(WatchcatConfig config) {
        super("Regen", "combat",
            config.getCheckWeight("combat.regen", 0.65),
            config.isCheckEnabled("combat.regen", true));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        double health = player.getHealth();
        if (health > lastHealth) {
            long now = System.currentTimeMillis();
            long interval = now - lastHealTime;
            lastHealTime = now;
            if (interval < 1000) {
                healCount++;
                if (healCount > 3) {
                    return CheckResult.CANCELLED;
                }
            } else {
                healCount = Math.max(0, healCount - 1);
            }
        }
        lastHealth = health;
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        double health = player.getHealth();
        if (health > lastHealth) {
            long now = System.currentTimeMillis();
            long interval = now - lastHealTime;
            lastHealTime = now;
            if (interval < 500) {
                healCount++;
                if (healCount > 5) {
                    healCount = 0;
                    return CheckResult.FLAG;
                }
            } else if (interval < 1000 && healCount > 0) {
                return CheckResult.FLAG;
            } else {
                healCount = Math.max(0, healCount - 1);
            }
        }
        lastHealth = health;
        return CheckResult.PASS;
    }
}
