package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class CrashPayloadCheck extends AbstractCheck {

    private int crashCount;

    private static final int MAX_CHANNEL_LENGTH = 64;
    private static final int MAX_PAYLOAD_SIZE = 32767;

    public CrashPayloadCheck(WatchcatConfig config) {
        super("CrashPayload", "mod",
            config.getCheckWeight("mod.crashpayload", 0.9),
            config.isCheckEnabled("mod.crashpayload", true));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundCustomPayloadPacket payload)) return CheckResult.PASS;
        String channel;
        int dataLen;
        try {
            channel = payload.getName();
            dataLen = payload.getData().readableBytes();
        } catch (Exception e) { return CheckResult.PASS; }

        if (channel != null && channel.length() > MAX_CHANNEL_LENGTH) {
            crashCount++;
            return CheckResult.CANCELLED;
        }
        if (dataLen > MAX_PAYLOAD_SIZE) {
            crashCount++;
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (crashCount > 0) {
            crashCount = Math.max(0, crashCount - 1);
        }
        return CheckResult.PASS;
    }
}
