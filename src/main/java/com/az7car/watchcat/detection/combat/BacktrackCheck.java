package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class BacktrackCheck extends AbstractCheck {

    private double lastTargetDistance;
    private int backtrackCount;
    private long lastAttackTime;

    public BacktrackCheck(WatchcatConfig config) {
        super("Backtrack", "combat",
            config.getCheckWeight("combat.backtrack", 0.7),
            config.isCheckEnabled("combat.backtrack", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        if (lastAttackTime > 0) {
            long interval = now - lastAttackTime;
            if (interval > 2000) {
                double ping = 0;
                try {
                    ping = nmsPlayer.connection.latency();
                } catch (Exception e) {}
                if (ping > 0 && interval > ping + 1000) {
                    backtrackCount++;
                    if (backtrackCount > 3) {
                        return CheckResult.FLAG;
                    }
                } else {
                    backtrackCount = Math.max(0, backtrackCount - 1);
                }
            }
        }
        lastAttackTime = now;
        return CheckResult.PASS;
    }
}
