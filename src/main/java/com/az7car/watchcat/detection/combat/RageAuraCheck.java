package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class RageAuraCheck extends AbstractCheck {

    private int rageAuraCount;
    private long lastInteractTime;

    public RageAuraCheck(WatchcatConfig config) {
        super("RageAura", "combat",
            config.getCheckWeight("combat.rageaura", 0.6),
            config.isCheckEnabled("combat.rageaura", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        if (lastInteractTime != 0 && now - lastInteractTime < 20) {
            rageAuraCount++;
            if (rageAuraCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            rageAuraCount = Math.max(0, rageAuraCount - 1);
        }
        lastInteractTime = now;
        return CheckResult.PASS;
    }
}
