package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AutoRespawnCheck extends AbstractCheck {

    private int respawnCount;
    private long lastDeath;

    public AutoRespawnCheck(WatchcatConfig config) {
        super("AutoRespawn", "world",
            config.getCheckWeight("world.autorespawn", 0.3),
            config.isCheckEnabled("world.autorespawn", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundClientCommandPacket cmd)) return CheckResult.PASS;
        if (cmd.getAction() == ServerboundClientCommandPacket.Action.PERFORM_RESPAWN) {
            long now = System.currentTimeMillis();
            if (lastDeath > 0 && (now - lastDeath) < 200) {
                respawnCount++;
                if (respawnCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                respawnCount = Math.max(0, respawnCount - 1);
            }
            lastDeath = now;
        }
        return CheckResult.PASS;
    }
}
