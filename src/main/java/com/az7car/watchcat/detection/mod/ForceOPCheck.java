package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;

public class ForceOPCheck extends AbstractCheck {

    private static final List<String> OP_CHANNELS = Arrays.asList(
        "OP", "op", "Op", "minecraft:op", "MC|OP",
        "forceop", "force-op", "admin", "sudo"
    );
    private int forceOpCount;

    public ForceOPCheck(WatchcatConfig config) {
        super("ForceOP", "mod",
            config.getCheckWeight("mod.forceop", 0.8),
            config.isCheckEnabled("mod.forceop", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundCustomPayloadPacket payload)) return CheckResult.PASS;
        String channel;
        try {
            Object p = payload.getClass().getMethod("payload").invoke(payload);
            Object id = p.getClass().getMethod("type").invoke(p);
            channel = id.toString();
        } catch (Exception e) { return CheckResult.PASS; }
        if (channel == null) return CheckResult.PASS;
        for (String op : OP_CHANNELS) {
            if (channel.equals(op)) {
                forceOpCount++;
                if (forceOpCount > 1) {
                    return CheckResult.FLAG;
                }
                return CheckResult.PASS;
            }
        }
        forceOpCount = Math.max(0, forceOpCount - 1);
        return CheckResult.PASS;
    }
}
