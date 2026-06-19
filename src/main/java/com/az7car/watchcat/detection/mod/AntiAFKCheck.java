package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AntiAFKCheck extends AbstractCheck {

    private long lastMovement;
    private int periodicActionCount;

    public AntiAFKCheck(WatchcatConfig config) {
        super("AntiAFK", "mod",
            config.getCheckWeight("mod.antiafk", 0.3),
            config.isCheckEnabled("mod.antiafk", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof net.minecraft.network.protocol.game.ServerboundMovePlayerPacket)) return CheckResult.PASS;

        double delta = data.getPositionDelta();
        long now = System.currentTimeMillis();

        if (delta > 0.01) {
            lastMovement = now;
            periodicActionCount = 0;
            return CheckResult.PASS;
        }

        if (data.getDeltaYaw() != 0 || data.getDeltaPitch() != 0) {
            if (delta < 0.001) {
                long idleTime = now - lastMovement;
                if (idleTime > 30000 && idleTime < 120000) {
                    periodicActionCount++;
                    if (periodicActionCount > 3) {
                        return CheckResult.FLAG;
                    }
                }
            }
        }

        return CheckResult.PASS;
    }
}
