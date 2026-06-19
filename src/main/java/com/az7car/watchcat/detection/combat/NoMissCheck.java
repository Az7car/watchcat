package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoMissCheck extends AbstractCheck {

    private int hitCount;
    private int noMissCount;

    public NoMissCheck(WatchcatConfig config) {
        super("NoMiss", "combat",
            config.getCheckWeight("combat.nomiss", 0.6),
            config.isCheckEnabled("combat.nomiss", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        hitCount++;
        if (hitCount > 50) {
            noMissCount++;
            if (noMissCount > 2) {
                noMissCount = 0;
                hitCount = 0;
                return CheckResult.FLAG;
            }
            hitCount = 0;
        }
        return CheckResult.PASS;
    }

    public void recordMiss() {
        hitCount = Math.max(0, hitCount - 2);
        noMissCount = 0;
    }
}
