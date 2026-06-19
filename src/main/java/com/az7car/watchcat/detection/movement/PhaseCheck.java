package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PhaseCheck extends AbstractCheck {

    private final double blockPenetrationTolerance;

    public PhaseCheck(WatchcatConfig config) {
        super("Phase", "movement",
            config.getCheckWeight("movement.phase"),
            config.isCheckEnabled("movement.phase"));
        this.blockPenetrationTolerance = config.getCheckDouble("movement.phase", "block-penetration-tolerance", 0.001);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        var loc = player.getLocation();
        var block = loc.getBlock();
        if (block.getType().isSolid() && block.getType() != org.bukkit.Material.LADDER
                && block.getType() != org.bukkit.Material.VINE) {
            double insideAmount = 0.5 - Math.abs(loc.getX() % 1 - 0.5);
            if (insideAmount > 0.3) {
                return CheckResult.CANCELLED;
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        var loc = player.getLocation();
        var block = loc.getBlock();
        if (block.getType().isSolid() && block.getType() != Material.LADDER
                && block.getType() != Material.VINE) {
            double insideAmount = 0.5 - Math.abs(loc.getX() % 1 - 0.5);
            if (insideAmount > blockPenetrationTolerance) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
