package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.*;

public class EntitySpeedCheck extends AbstractCheck {

    public EntitySpeedCheck(WatchcatConfig config) {
        super("EntitySpeed", "movement",
            config.getCheckWeight("movement.entityspeed", 0.5),
            config.isCheckEnabled("movement.entityspeed", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!player.isInsideVehicle()) return CheckResult.PASS;
        Entity vehicle = player.getVehicle();

        double maxSpeed = 0.6;
        if (vehicle instanceof Horse) maxSpeed = 0.45;
        else if (vehicle instanceof Pig) maxSpeed = 0.4;
        else if (vehicle instanceof Minecart) maxSpeed = 0.4;
        else if (vehicle instanceof Boat) maxSpeed = 0.6;

        double horizontal = data.getHorizontalPositionDelta();
        if (horizontal > maxSpeed * 1.5) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
