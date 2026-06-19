package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class FastCloseCheck extends AbstractCheck {

    private int fastCloseCount;
    private long lastOpenTime;

    public FastCloseCheck(WatchcatConfig config) {
        super("FastClose", "world",
            config.getCheckWeight("world.fastclose", 0.35),
            config.isCheckEnabled("world.fastclose", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundContainerClosePacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        if (lastOpenTime != 0 && now - lastOpenTime < 50) {
            fastCloseCount++;
            if (fastCloseCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            fastCloseCount = Math.max(0, fastCloseCount - 1);
        }
        return CheckResult.PASS;
    }

    public void markOpen() { lastOpenTime = System.currentTimeMillis(); }
}
