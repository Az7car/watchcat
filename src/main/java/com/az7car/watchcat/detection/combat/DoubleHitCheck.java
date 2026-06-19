package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class DoubleHitCheck extends AbstractCheck {

    private long lastHitTime;
    private int doubleHitCount;

    public DoubleHitCheck(WatchcatConfig config) {
        super("DoubleHit", "combat",
            config.getCheckWeight("combat.doublehit", 0.55),
            config.isCheckEnabled("combat.doublehit", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        if (lastHitTime > 0 && (now - lastHitTime) < 10) {
            doubleHitCount++;
            if (doubleHitCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            doubleHitCount = Math.max(0, doubleHitCount - 1);
        }
        lastHitTime = now;
        return CheckResult.PASS;
    }
}
