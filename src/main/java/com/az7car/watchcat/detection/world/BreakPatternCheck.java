package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BreakPatternCheck extends AbstractCheck {

    private int veinCount;
    private int sequentialCount;
    private int lastX, lastY, lastZ;

    public BreakPatternCheck(WatchcatConfig config) {
        super("BreakPattern", "world",
            config.getCheckWeight("world.breakpattern", 0.5),
            config.isCheckEnabled("world.breakpattern", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return CheckResult.PASS;

        int x = action.getPos().getX();
        int y = action.getPos().getY();
        int z = action.getPos().getZ();

        if (lastX != 0 || lastY != 0 || lastZ != 0) {
            boolean adjacent = Math.abs(x - lastX) <= 1 && Math.abs(y - lastY) <= 1 && Math.abs(z - lastZ) <= 1;
            if (adjacent) {
                sequentialCount++;
                if (sequentialCount > 20) {
                    return CheckResult.FLAG;
                }
            } else {
                sequentialCount = Math.max(0, sequentialCount - 1);
            }
        }
        lastX = x; lastY = y; lastZ = z;
        return CheckResult.PASS;
    }
}
