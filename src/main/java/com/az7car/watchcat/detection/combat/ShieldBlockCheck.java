package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ShieldBlockCheck extends AbstractCheck {

    private long lastShieldToggle;
    private int fastToggleCount;

    public ShieldBlockCheck(WatchcatConfig config) {
        super("ShieldBlock", "combat",
            config.getCheckWeight("combat.shieldblock", 0.5),
            config.isCheckEnabled("combat.shieldblock", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerCommandPacket cmd)) return CheckResult.PASS;
        if (cmd.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING ||
            cmd.getAction() == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
            return CheckResult.PASS;
        }
        long now = System.currentTimeMillis();
        if (lastShieldToggle > 0) {
            long interval = now - lastShieldToggle;
            if (interval < 50) {
                fastToggleCount++;
                if (fastToggleCount > 5) {
                    return CheckResult.FLAG;
                }
            } else {
                fastToggleCount = Math.max(0, fastToggleCount - 1);
            }
        }
        lastShieldToggle = now;
        return CheckResult.PASS;
    }
}
