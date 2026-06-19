package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class PacketDelayCheck extends AbstractCheck {

    private long lastPacketTime;
    private int packetDelayCount;

    public PacketDelayCheck(WatchcatConfig config) {
        super("PacketDelay", "movement",
            config.getCheckWeight("movement.packetdelay", 0.5),
            config.isCheckEnabled("movement.packetdelay", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        long now = System.currentTimeMillis();
        if (lastPacketTime != 0) {
            long delta = now - lastPacketTime;
            if (delta > 3000) {
                packetDelayCount++;
                if (packetDelayCount > 2) {
                    return CheckResult.FLAG;
                }
            } else {
                packetDelayCount = Math.max(0, packetDelayCount - 1);
            }
        }
        lastPacketTime = now;
        return CheckResult.PASS;
    }
}
