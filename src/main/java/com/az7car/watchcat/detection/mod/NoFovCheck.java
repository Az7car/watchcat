package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoFovCheck extends AbstractCheck {

    private int noFovCount;
    private boolean isSpeeding;

    public NoFovCheck(WatchcatConfig config) {
        super("NoFov", "mod",
            config.getCheckWeight("mod.nofov", 0.3),
            config.isCheckEnabled("mod.nofov", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        boolean sprinting = data.isSprinting();
        boolean hadSpeed = player.isSprinting() || player.getWalkSpeed() > 0.2f;
        if (sprinting && !hadSpeed) {
            noFovCount++;
            if (noFovCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            noFovCount = Math.max(0, noFovCount - 1);
        }
        return CheckResult.PASS;
    }
}
