package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoFireCheck extends AbstractCheck {

    private int noFireCount;
    private long lastFireTick;

    public NoFireCheck(WatchcatConfig config) {
        super("NoFire", "combat",
            config.getCheckWeight("combat.nofire", 0.4),
            config.isCheckEnabled("combat.nofire", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        boolean isOnFire = player.getFireTicks() > 0;
        if (isOnFire) {
            lastFireTick = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - lastFireTick < 100) {
            boolean hasFireProt = player.getFireTicks() == 0;
            if (hasFireProt) {
                noFireCount++;
                if (noFireCount > 3) {
                    return CheckResult.FLAG;
                }
            }
        }
        if (noFireCount > 0 && !isOnFire) {
            noFireCount = Math.max(0, noFireCount - 1);
        }
        return CheckResult.PASS;
    }
}
