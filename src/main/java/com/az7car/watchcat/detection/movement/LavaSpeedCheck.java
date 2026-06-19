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

public class LavaSpeedCheck extends AbstractCheck {

    private final double maxLavaSpeed;
    private int lavaSpeedCount;

    public LavaSpeedCheck(WatchcatConfig config) {
        super("LavaSpeed", "movement",
            config.getCheckWeight("movement.lavaspeed", 0.55),
            config.isCheckEnabled("movement.lavaspeed", true));
        this.maxLavaSpeed = config.getCheckDouble("movement.lavaspeed", "max-speed", 0.1);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        Material feet = player.getLocation().getBlock().getType();
        if (feet != Material.LAVA) return CheckResult.PASS;

        double dx = data.getDeltaX();
        double dz = data.getDeltaZ();
        double dh = Math.sqrt(dx * dx + dz * dz);

        if (dh > maxLavaSpeed) {
            lavaSpeedCount++;
            if (lavaSpeedCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            lavaSpeedCount = Math.max(0, lavaSpeedCount - 1);
        }
        return CheckResult.PASS;
    }
}
