package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class WaterSpeedCheck extends AbstractCheck {

    private final double maxWaterSpeed;
    private int waterSpeedCount;

    public WaterSpeedCheck(WatchcatConfig config) {
        super("WaterSpeed", "movement",
            config.getCheckWeight("movement.waterspeed", 0.55),
            config.isCheckEnabled("movement.waterspeed", true));
        this.maxWaterSpeed = config.getCheckDouble("movement.waterspeed", "max-speed", 0.15);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        Material head = player.getEyeLocation().getBlock().getType();
        if (!head.name().contains("WATER")) return CheckResult.PASS;

        double dx = data.getPositionDelta().getX();
        double dz = data.getPositionDelta().getZ();
        double dh = Math.sqrt(dx * dx + dz * dz);

        if (dh > maxWaterSpeed) {
            waterSpeedCount++;
            if (waterSpeedCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            waterSpeedCount = Math.max(0, waterSpeedCount - 1);
        }
        return CheckResult.PASS;
    }
}
