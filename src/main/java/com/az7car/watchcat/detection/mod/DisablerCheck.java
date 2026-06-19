package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class DisablerCheck extends AbstractCheck {

    private long lastKeepAliveId;
    private int invalidKeepAlives;

    public DisablerCheck(WatchcatConfig config) {
        super("Disabler", "mod",
            config.getCheckWeight("mod.disabler", 0.8),
            config.isCheckEnabled("mod.disabler", true));
        this.lastKeepAliveId = -1;
        this.invalidKeepAlives = 0;
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundKeepAlivePacket keepAlive) {
            long id = keepAlive.getId();
            if (id == 0 && lastKeepAliveId > 0) {
                invalidKeepAlives++;
                if (invalidKeepAlives > 3) {
                    return CheckResult.CANCELLED;
                }
            }
            lastKeepAliveId = id;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundKeepAlivePacket keepAlive) {
            long id = keepAlive.getId();
            if (id == 0 && lastKeepAliveId > 0) {
                invalidKeepAlives++;
                if (invalidKeepAlives > 2) {
                    return CheckResult.FLAG;
                }
            } else if (id > lastKeepAliveId) {
                invalidKeepAlives = 0;
            }
            lastKeepAliveId = id;
        }
        return CheckResult.PASS;
    }
}
