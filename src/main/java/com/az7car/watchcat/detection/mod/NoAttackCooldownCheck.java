package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoAttackCooldownCheck extends AbstractCheck {

    private int noCooldownCount;
    private long lastAttackTime;

    public NoAttackCooldownCheck(WatchcatConfig config) {
        super("NoAttackCooldown", "mod",
            config.getCheckWeight("mod.noattackcooldown", 0.6),
            config.isCheckEnabled("mod.noattackcooldown", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundSwingPacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        long attackDiff = now - data.getLastAttackTime();
        if (attackDiff < 100) {
            noCooldownCount++;
            if (noCooldownCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            noCooldownCount = Math.max(0, noCooldownCount - 1);
        }
        return CheckResult.PASS;
    }
}
