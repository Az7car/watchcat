package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class FastLadderCheck extends AbstractCheck {

    private final double maxLadderSpeed;

    public FastLadderCheck(WatchcatConfig config) {
        super("FastLadder", "movement",
            config.getCheckWeight("movement.fastladder"),
            config.isCheckEnabled("movement.fastladder"));
        this.maxLadderSpeed = config.getCheckDouble("movement.fastladder", "max-ladder-speed", 0.15);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        Block feet = player.getLocation().getBlock();
        boolean onLadder = feet.getType() == Material.LADDER
            || feet.getType() == Material.VINE;

        if (!onLadder) return CheckResult.PASS;

        double deltaY = data.getDeltaY();
        if (deltaY > maxLadderSpeed) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
