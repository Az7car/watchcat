package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SafeWalkCheck extends AbstractCheck {

    public SafeWalkCheck(WatchcatConfig config) {
        super("SafeWalk", "movement",
            config.getCheckWeight("movement.safewalk", 0.3),
            config.isCheckEnabled("movement.safewalk", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.isSneaking() || data.isOnGround()) return CheckResult.PASS;

        Location loc = player.getLocation();
        boolean atEdge = loc.clone().add(0, -0.5, 0).getBlock().getType() == Material.AIR;

        if (atEdge && !data.isSneaking() && data.getHorizontalPositionDelta() > 0 && data.isOnGround()) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
