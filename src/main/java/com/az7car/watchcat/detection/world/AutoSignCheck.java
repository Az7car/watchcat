package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AutoSignCheck extends AbstractCheck {

    private int autoSignCount;

    public AutoSignCheck(WatchcatConfig config) {
        super("AutoSign", "world",
            config.getCheckWeight("world.autosign", 0.4),
            config.isCheckEnabled("world.autosign", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundSignUpdatePacket sign)) return CheckResult.PASS;
        String[] lines = sign.getLines();
        long now = System.currentTimeMillis();
        long timeSinceLastBlock = now - data.getLastBlockPlaceTime();
        if (timeSinceLastBlock < 50 && lines != null && lines.length == 4) {
            boolean allNonEmpty = true;
            for (String l : lines) {
                if (l == null || l.isEmpty()) { allNonEmpty = false; break; }
            }
            if (allNonEmpty) {
                autoSignCount++;
                if (autoSignCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                autoSignCount = Math.max(0, autoSignCount - 1);
            }
        }
        return CheckResult.PASS;
    }
}
