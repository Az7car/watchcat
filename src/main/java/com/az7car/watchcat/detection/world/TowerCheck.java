package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class TowerCheck extends AbstractCheck {

    private final double maxTowerSpeed;

    public TowerCheck(WatchcatConfig config) {
        super("Tower", "world",
            config.getCheckWeight("world.tower"),
            config.isCheckEnabled("world.tower"));
        this.maxTowerSpeed = config.getCheckDouble("world.tower", "max-tower-speed", 0.8);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof net.minecraft.network.protocol.game.ServerboundUseItemOnPacket)) return CheckResult.PASS;
        double deltaY = data.getDeltaY();
        if (deltaY > maxTowerSpeed * 1.5) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof net.minecraft.network.protocol.game.ServerboundUseItemOnPacket)) return CheckResult.PASS;

        double deltaY = data.getDeltaY();
        double horizontal = data.getHorizontalPositionDelta();

        if (deltaY > maxTowerSpeed && horizontal < 0.1) {
            return CheckResult.FLAG;
        }

        if (deltaY > 0 && data.isOnGround()) {
            long now = System.currentTimeMillis();
            long lastPlace = data.getLastBlockPlaceTime();
            if (lastPlace > 0 && (now - lastPlace) < 100) {
                if (horizontal < 0.05 && deltaY > 0.3) {
                    return CheckResult.FLAG;
                }
            }
        }

        return CheckResult.PASS;
    }
}
