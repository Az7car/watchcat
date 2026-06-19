package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BlockInteractionCheck extends AbstractCheck {

    private final double maxInteractionDistance;
    private int interactionCount;

    public BlockInteractionCheck(WatchcatConfig config) {
        super("BlockInteraction", "world",
            config.getCheckWeight("world.blockinteraction", 0.6),
            config.isCheckEnabled("world.blockinteraction", true));
        this.maxInteractionDistance = config.getCheckDouble("world.blockinteraction", "max-distance", 6.0);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return CheckResult.PASS;
        Location eye = player.getEyeLocation();
        double bx = action.getPos().getX() + 0.5;
        double by = action.getPos().getY() + 0.5;
        double bz = action.getPos().getZ() + 0.5;
        double dist = eye.distance(new Location(player.getWorld(), bx, by, bz));
        if (dist > maxInteractionDistance + 1) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        Location eye = player.getEyeLocation();
        double bx = action.getPos().getX() + 0.5;
        double by = action.getPos().getY() + 0.5;
        double bz = action.getPos().getZ() + 0.5;
        double dist = eye.distance(new Location(player.getWorld(), bx, by, bz));
        if (dist > maxInteractionDistance) {
            interactionCount++;
            if (interactionCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            interactionCount = Math.max(0, interactionCount - 1);
        }
        return CheckResult.PASS;
    }
}
