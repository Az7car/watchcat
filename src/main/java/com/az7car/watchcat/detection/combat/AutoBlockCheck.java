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

public class AutoBlockCheck extends AbstractCheck {

    private int blockTimingCount;
    private long lastInteractTime;
    private boolean interactAfterSwing;

    public AutoBlockCheck(WatchcatConfig config) {
        super("AutoBlock", "combat",
            config.getCheckWeight("combat.autoblock", 0.5),
            config.isCheckEnabled("combat.autoblock", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        long now = System.currentTimeMillis();

        if (packet instanceof ServerboundSwingPacket) {
            interactAfterSwing = false;
        }

        if (packet instanceof ServerboundInteractPacket) {
            if (player.isHandRaised() && player.getInventory().getItemInMainHand().getType().name().contains("SHIELD")) {
                if (interactAfterSwing) {
                    blockTimingCount++;
                    if (blockTimingCount > 10) {
                        return CheckResult.FLAG;
                    }
                } else {
                    blockTimingCount = Math.max(0, blockTimingCount - 1);
                }
            }
            interactAfterSwing = true;
        }
        return CheckResult.PASS;
    }
}
