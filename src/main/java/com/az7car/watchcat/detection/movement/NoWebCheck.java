package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class NoWebCheck extends AbstractCheck {

    private final double minWebSpeedReduction;

    public NoWebCheck(WatchcatConfig config) {
        super("NoWeb", "movement",
            config.getCheckWeight("movement.noweb"),
            config.isCheckEnabled("movement.noweb"));
        this.minWebSpeedReduction = config.getCheckDouble("movement.noweb", "min-web-speed-reduction", 0.2);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (player.getLocation().getBlock().getType() != Material.COBWEB) return CheckResult.PASS;

        double horizontal = data.getHorizontalPositionDelta();
        if (horizontal > minWebSpeedReduction) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
