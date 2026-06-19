package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;

public class NoPushCheck extends AbstractCheck {

    private int noPushCount;

    public NoPushCheck(WatchcatConfig config) {
        super("NoPush", "combat",
            config.getCheckWeight("combat.nopush", 0.45),
            config.isCheckEnabled("combat.nopush", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        boolean nearEntity = !player.getNearbyEntities(0.5, 0.5, 0.5).isEmpty();
        boolean inWater = player.isInWaterOrBubbleColumn();
        double hDelta = data.getHorizontalPositionDelta();

        if ((nearEntity || inWater) && hDelta < 0.001) {
            noPushCount++;
            if (noPushCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            noPushCount = Math.max(0, noPushCount - 1);
        }
        return CheckResult.PASS;
    }
}
