package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoBobCheck extends AbstractCheck {

    private int noBobCount;

    public NoBobCheck(WatchcatConfig config) {
        super("NoBob", "mod",
            config.getCheckWeight("mod.nobob", 0.3),
            config.isCheckEnabled("mod.nobob", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!data.isOnGround() && data.getAirTicks() > 5) return CheckResult.PASS;
        float dp = data.getDeltaPitch();
        if (Math.abs(dp) < 0.001f && data.getHorizontalVelocity() > 0.1) {
            noBobCount++;
            if (noBobCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            noBobCount = Math.max(0, noBobCount - 1);
        }
        return CheckResult.PASS;
    }
}
