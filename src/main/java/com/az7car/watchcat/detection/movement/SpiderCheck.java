package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;

public class SpiderCheck extends AbstractCheck {

    private final double maxClimbSpeed;

    public SpiderCheck(WatchcatConfig config) {
        super("Spider", "movement",
            config.getCheckWeight("movement.spider"),
            config.isCheckEnabled("movement.spider"));
        this.maxClimbSpeed = config.getCheckDouble("movement.spider", "max-climb-speed", 0.2);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.isOnGround()) return CheckResult.PASS;

        Block below = player.getLocation().subtract(0, 0.1, 0).getBlock();
        boolean againstWall = below.getType() == Material.AIR
            && player.getLocation().add(0, 1, 0).getBlock().getType() != Material.AIR
            && !player.isInWater() && !player.isInLava();

        if (!againstWall) return CheckResult.PASS;

        double deltaY = data.getDeltaY();
        if (deltaY > maxClimbSpeed) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
