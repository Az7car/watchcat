package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ClickPatternCheck extends AbstractCheck {

    private int clickPatternCount;
    private long lastClickTime;
    private long lastInterval;

    public ClickPatternCheck(WatchcatConfig config) {
        super("ClickPattern", "combat",
            config.getCheckWeight("combat.clickpattern", 0.5),
            config.isCheckEnabled("combat.clickpattern", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        data.recordClick();
        long now = System.currentTimeMillis();
        if (lastClickTime != 0) {
            long interval = now - lastClickTime;
            if (lastInterval != 0) {
                long diff = Math.abs(interval - lastInterval);
                if (diff < 2 && interval > 0) {
                    clickPatternCount++;
                    if (clickPatternCount > 5) {
                        return CheckResult.FLAG;
                    }
                } else {
                    clickPatternCount = Math.max(0, clickPatternCount - 1);
                }
            }
            lastInterval = interval;
        }
        lastClickTime = now;
        return CheckResult.PASS;
    }
}
