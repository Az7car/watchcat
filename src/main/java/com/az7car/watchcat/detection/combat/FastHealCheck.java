package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class FastHealCheck extends AbstractCheck {

    private final long minHealInterval;
    private long lastHealTime;

    public FastHealCheck(WatchcatConfig config) {
        super("FastHeal", "combat",
            config.getCheckWeight("combat.fastheal", 0.6),
            config.isCheckEnabled("combat.fastheal", true));
        this.minHealInterval = (long) config.getCheckDouble("combat.fastheal", "min-heal-interval", 500);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        if (player.getHealth() >= player.getMaxHealth()) return CheckResult.PASS;
        long interval = System.currentTimeMillis() - lastHealTime;
        data.recordReach(interval);
        lastHealTime = System.currentTimeMillis();
        if (interval < minHealInterval) {
            return CheckResult.FLAG;
        }
        return CheckResult.PASS;
    }
}
