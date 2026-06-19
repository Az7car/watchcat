package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class DirectionCheck extends AbstractCheck {

    private int invalidDirectionCount;

    public DirectionCheck(WatchcatConfig config) {
        super("Direction", "world",
            config.getCheckWeight("world.direction", 0.5),
            config.isCheckEnabled("world.direction", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;

        Direction dir = action.getDirection();
        float pitch = data.getLastPitch();

        if (dir == Direction.UP && pitch > 70) {
            invalidDirectionCount++;
            if (invalidDirectionCount > 5) {
                return CheckResult.FLAG;
            }
        } else if (dir == Direction.DOWN && pitch < -70) {
            invalidDirectionCount++;
            if (invalidDirectionCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            invalidDirectionCount = Math.max(0, invalidDirectionCount - 1);
        }
        return CheckResult.PASS;
    }
}
