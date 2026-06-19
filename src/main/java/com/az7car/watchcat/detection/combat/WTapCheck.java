package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class WTapCheck extends AbstractCheck {

    private final long maxLegitWTapWindow;
    private long lastSprintToggleTime;
    private boolean wasSprinting;
    private int perfectWTapCount;

    public WTapCheck(WatchcatConfig config) {
        super("WTap", "combat",
            config.getCheckWeight("combat.wtap", 0.5),
            config.isCheckEnabled("combat.wtap", true));
        this.maxLegitWTapWindow = (long) config.getCheckDouble("combat.wtap", "max-wtap-window", 100);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerCommandPacket cmd)) return CheckResult.PASS;
        if (cmd.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING ||
            cmd.getAction() == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
            long now = System.currentTimeMillis();
            if (lastSprintToggleTime > 0) {
                long interval = now - lastSprintToggleTime;
                if (interval < maxLegitWTapWindow) {
                    perfectWTapCount++;
                    if (perfectWTapCount > 20) {
                        return CheckResult.FLAG;
                    }
                } else {
                    perfectWTapCount = Math.max(0, perfectWTapCount - 1);
                }
            }
            lastSprintToggleTime = now;
        }
        return CheckResult.PASS;
    }
}
