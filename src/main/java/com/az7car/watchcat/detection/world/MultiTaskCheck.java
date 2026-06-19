package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class MultiTaskCheck extends AbstractCheck {

    private int multiTaskCount;
    private boolean wasAttacking;
    private boolean wasEating;

    public MultiTaskCheck(WatchcatConfig config) {
        super("MultiTask", "world",
            config.getCheckWeight("world.multitask", 0.5),
            config.isCheckEnabled("world.multitask", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundUseItemPacket) {
            wasEating = true;
        }
        if (packet instanceof ServerboundPlayerActionPacket action) {
            if (action.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                if (wasEating) {
                    multiTaskCount++;
                    if (multiTaskCount > 3) {
                        return CheckResult.FLAG;
                    }
                    wasEating = false;
                } else {
                    multiTaskCount = Math.max(0, multiTaskCount - 1);
                }
                wasAttacking = true;
            } else {
                wasAttacking = false;
            }
        }
        return CheckResult.PASS;
    }
}
