package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class PacketSpamCheck extends AbstractCheck {

    private int spamCount;
    private long lastPacketTime;
    private int packetBurst;

    public PacketSpamCheck(WatchcatConfig config) {
        super("PacketSpam", "mod",
            config.getCheckWeight("mod.packetspam", 0.4),
            config.isCheckEnabled("mod.packetspam", true));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        long now = System.currentTimeMillis();
        if (now - lastPacketTime < 5) {
            packetBurst++;
            if (packetBurst > 20) {
                spamCount++;
                return CheckResult.CANCELLED;
            }
        } else {
            packetBurst = 0;
        }
        lastPacketTime = now;
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (spamCount > 0) spamCount = Math.max(0, spamCount - 1);
        return CheckResult.PASS;
    }
}
