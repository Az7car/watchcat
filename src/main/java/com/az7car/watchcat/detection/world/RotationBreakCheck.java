package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class RotationBreakCheck extends AbstractCheck {

    private int impossibleBreakCount;

    public RotationBreakCheck(WatchcatConfig config) {
        super("RotationBreak", "world",
            config.getCheckWeight("world.rotationbreak", 0.5),
            config.isCheckEnabled("world.rotationbreak", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        float pitch = data.getLastPitch();

        boolean lookingUp = pitch < -80;
        boolean lookingDown = pitch > 80;
        int by = action.getPos().getY();

        if (player.getLocation().getBlockY() < by && lookingDown) {
            impossibleBreakCount++;
            if (impossibleBreakCount > 3) {
                return CheckResult.FLAG;
            }
        } else if (player.getLocation().getBlockY() > by && lookingUp) {
            impossibleBreakCount++;
            if (impossibleBreakCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            impossibleBreakCount = Math.max(0, impossibleBreakCount - 1);
        }
        return CheckResult.PASS;
    }
}
