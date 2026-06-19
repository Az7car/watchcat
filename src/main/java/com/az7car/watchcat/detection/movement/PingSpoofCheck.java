package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class PingSpoofCheck extends AbstractCheck {

    private long lastPacketTime;
    private int pingSpoofCount;

    public PingSpoofCheck(WatchcatConfig config) {
        super("PingSpoof", "movement",
            config.getCheckWeight("movement.pingspoof", 0.5),
            config.isCheckEnabled("movement.pingspoof", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        if (lastPacketTime != 0) {
            long delta = now - lastPacketTime;
            if (delta > 2000) {
                pingSpoofCount++;
                if (pingSpoofCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                pingSpoofCount = Math.max(0, pingSpoofCount - 1);
            }
        }
        lastPacketTime = now;
        return CheckResult.PASS;
    }
}
