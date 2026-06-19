package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ReachMultiCheck extends AbstractCheck {

    private int lastTargetId;
    private int multiTargetCount;
    private long lastAttackTime;

    public ReachMultiCheck(WatchcatConfig config) {
        super("ReachMulti", "combat",
            config.getCheckWeight("combat.reachmulti", 0.6),
            config.isCheckEnabled("combat.reachmulti", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        if (lastTargetId != 0 && interact.getEntityId() != lastTargetId) {
            if ((now - lastAttackTime) < 50) {
                multiTargetCount++;
                if (multiTargetCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                multiTargetCount = Math.max(0, multiTargetCount - 1);
            }
        }
        lastTargetId = interact.getEntityId();
        lastAttackTime = now;
        return CheckResult.PASS;
    }
}
