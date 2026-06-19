package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AutoMineCheck extends AbstractCheck {

    public AutoMineCheck(WatchcatConfig config) {
        super("AutoMine", "world",
            config.getCheckWeight("world.automine", 0.6),
            config.isCheckEnabled("world.automine", true));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket actionPacket)) return CheckResult.PASS;
        if (actionPacket.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            long now = System.currentTimeMillis();
            if (data.getLastBlockPlaceTime() > 0) {
                long sinceLast = now - data.getLastBlockPlaceTime();
                if (sinceLast < 20) {
                    return CheckResult.CANCELLED;
                }
            }
            double deltaYaw = Math.abs(data.getDeltaYaw());
            double deltaPitch = Math.abs(data.getDeltaPitch());
            if (deltaYaw < 0.1 && deltaPitch < 0.1) {
                data.recordClick();
                if (data.getClickTimestamps().size() > 15) {
                    return CheckResult.CANCELLED;
                }
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket actionPacket)) return CheckResult.PASS;

        if (actionPacket.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            long now = System.currentTimeMillis();
            if (data.getLastBlockPlaceTime() > 0) {
                long sinceLast = now - data.getLastBlockPlaceTime();
                if (sinceLast < 50) {
                    return CheckResult.FLAG;
                }
            }

            double deltaYaw = Math.abs(data.getDeltaYaw());
            double deltaPitch = Math.abs(data.getDeltaPitch());

            if (deltaYaw < 0.5 && deltaPitch < 0.5) {
                data.recordClick();
                if (data.getClickTimestamps().size() > 10) {
                    return CheckResult.FLAG;
                }
            }
        }

        return CheckResult.PASS;
    }
}
