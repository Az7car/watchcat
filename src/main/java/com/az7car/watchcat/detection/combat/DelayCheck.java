package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class DelayCheck extends AbstractCheck {

    private long lastSwingTime;
    private long lastAttackTime;
    private int delayViolationCount;

    public DelayCheck(WatchcatConfig config) {
        super("Delay", "combat",
            config.getCheckWeight("combat.delay", 0.5),
            config.isCheckEnabled("combat.delay", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        long now = System.currentTimeMillis();

        if (packet instanceof ServerboundSwingPacket) {
            lastSwingTime = now;
            if (lastAttackTime > 0) {
                long swingToAttack = now - lastAttackTime;
                if (swingToAttack < 10) {
                    delayViolationCount++;
                    if (delayViolationCount > 5) {
                        return CheckResult.FLAG;
                    }
                } else {
                    delayViolationCount = Math.max(0, delayViolationCount - 1);
                }
            }
        }

        if (packet instanceof ServerboundInteractPacket) {
            lastAttackTime = now;
        }

        return CheckResult.PASS;
    }
}
