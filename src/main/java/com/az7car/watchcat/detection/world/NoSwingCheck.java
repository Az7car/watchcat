package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoSwingCheck extends AbstractCheck {

    public NoSwingCheck(WatchcatConfig config) {
        super("NoSwing", "world",
            config.getCheckWeight("world.noswing"),
            config.isCheckEnabled("world.noswing"));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        boolean isInteract = packet instanceof ServerboundInteractPacket;
        boolean isAction = packet instanceof ServerboundPlayerActionPacket;
        if (!isInteract && !isAction) return CheckResult.PASS;
        int swingDiff = data.getSwingCount() - data.getLastSwingCount();
        if (swingDiff == 0 && player.getTicksLived() > 100) {
            data.recordClick();
            if (data.getClickTimestamps().size() > 20) {
                return CheckResult.CANCELLED;
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        boolean isInteract = packet instanceof ServerboundInteractPacket;
        boolean isAction = packet instanceof ServerboundPlayerActionPacket;

        if (!isInteract && !isAction) return CheckResult.PASS;

        int swingDiff = data.getSwingCount() - data.getLastSwingCount();
        data.setSwingCount(data.getSwingCount());

        if (swingDiff == 0 && player.getTicksLived() > 100) {
            data.recordClick();
            if (data.getClickTimestamps().size() > 5) {
                return CheckResult.FLAG;
            }
        }

        data.setSwingCount(data.getSwingCount());
        return CheckResult.PASS;
    }
}
