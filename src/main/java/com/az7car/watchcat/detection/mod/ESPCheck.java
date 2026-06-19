package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class ESPCheck extends AbstractCheck {

    private int espCount;

    public ESPCheck(WatchcatConfig config) {
        super("ESP", "mod",
            config.getCheckWeight("mod.esp", 0.5),
            config.isCheckEnabled("mod.esp", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        var target = player.getTargetEntityExact(6);
        if (target == null) {
            espCount = Math.max(0, espCount - 1);
            return CheckResult.PASS;
        }
        BlockIterator blocks = new BlockIterator(player.getWorld(),
            player.getLocation().toVector(),
            player.getLocation().getDirection(),
            0, 6);
        int wallCount = 0;
        while (blocks.hasNext()) {
            var block = blocks.next().getBlock();
            if (block.getType().isOccluding()) wallCount++;
        }
        if (wallCount > 1) {
            espCount++;
            if (espCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            espCount = Math.max(0, espCount - 1);
        }
        return CheckResult.PASS;
    }
}
