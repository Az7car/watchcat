package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class FastBreakCheck extends AbstractCheck {

    private final double maxBreakSpeed;

    public FastBreakCheck(WatchcatConfig config) {
        super("FastBreak", "world",
            config.getCheckWeight("world.fastbreak"),
            config.isCheckEnabled("world.fastbreak"));
        this.maxBreakSpeed = config.getCheckDouble("world.fastbreak", "max-break-speed", 0.5);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof net.minecraft.network.protocol.game.ServerboundPlayerActionPacket actionPacket))
            return CheckResult.PASS;
        if (actionPacket.getAction() == net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            long now = System.currentTimeMillis();
            Long lastBreak = data.getClickTimestamps().peekLast();
            if (lastBreak != null && lastBreak > 0) {
                long interval = now - lastBreak;
                if (interval < 20) return CheckResult.CANCELLED;
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof net.minecraft.network.protocol.game.ServerboundPlayerActionPacket actionPacket))
            return CheckResult.PASS;

        if (actionPacket.getAction() == net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            long now = System.currentTimeMillis();
            Long lastClick = data.getClickTimestamps().peekLast();
            if (lastClick != null) {
                long interval = now - lastClick;
                double breakSpeed = 1000.0 / (interval + 1);
                if (breakSpeed > maxBreakSpeed * 5) {
                    return CheckResult.FLAG;
                }
            }
            data.recordClick();
        }

        return CheckResult.PASS;
    }
}
