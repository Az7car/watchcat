package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class PerfectBlockCheck extends AbstractCheck {

    private int perfectBlockCount;

    public PerfectBlockCheck(WatchcatConfig config) {
        super("PerfectBlock", "combat",
            config.getCheckWeight("combat.perfectblock", 0.5),
            config.isCheckEnabled("combat.perfectblock", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundInteractPacket) {
            if (data.isBlocking()) {
                perfectBlockCount++;
                if (perfectBlockCount > 20) {
                    return CheckResult.FLAG;
                }
            } else {
                perfectBlockCount = Math.max(0, perfectBlockCount - 1);
            }
        }
        return CheckResult.PASS;
    }
}
