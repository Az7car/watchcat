package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class PacketOrderCheck extends AbstractCheck {

    private boolean expectingRelease;
    private int orderViolations;
    private boolean wasEating;

    public PacketOrderCheck(WatchcatConfig config) {
        super("PacketOrder", "world",
            config.getCheckWeight("world.packetorder", 0.5),
            config.isCheckEnabled("world.packetorder", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundUseItemPacket) {
            wasEating = true;
            return CheckResult.PASS;
        }

        if (packet instanceof ServerboundInteractPacket) {
            if (wasEating) {
                orderViolations++;
                if (orderViolations > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                orderViolations = Math.max(0, orderViolations - 1);
            }
            wasEating = false;
            return CheckResult.PASS;
        }

        if (packet instanceof ServerboundPlayerActionPacket action) {
            if (action.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) {
                expectingRelease = true;
                wasEating = false;
            }
            if (wasEating && action.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                orderViolations++;
                if (orderViolations > 3) {
                    return CheckResult.FLAG;
                }
            }
        }
        return CheckResult.PASS;
    }
}
