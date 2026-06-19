package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AutoRodCheck extends AbstractCheck {

    private int autoRodCount;
    private long lastRodTime;

    public AutoRodCheck(WatchcatConfig config) {
        super("AutoRod", "combat",
            config.getCheckWeight("combat.autorod", 0.4),
            config.isCheckEnabled("combat.autorod", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        boolean isRod = player.getInventory().getItemInMainHand().getType().name().contains("FISHING_ROD");
        if (!isRod) return CheckResult.PASS;
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        if (lastRodTime != 0 && now - lastRodTime < 200) {
            autoRodCount++;
            if (autoRodCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            autoRodCount = Math.max(0, autoRodCount - 1);
        }
        lastRodTime = now;
        return CheckResult.PASS;
    }
}
