package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.Material;

public class IceSpeedCheck extends AbstractCheck {

    private final double maxIceSpeed;
    private int iceSpeedCount;

    public IceSpeedCheck(WatchcatConfig config) {
        super("IceSpeed", "movement",
            config.getCheckWeight("movement.icespeed", 0.55),
            config.isCheckEnabled("movement.icespeed", true));
        this.maxIceSpeed = config.getCheckDouble("movement.icespeed", "max-speed", 0.8);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        Material below = player.getLocation().subtract(0, 0.5, 0).getBlock().getType();
        if (!below.name().contains("ICE") && !below.name().contains("FROSTED")) return CheckResult.PASS;
        if (move.isOnGround()) return CheckResult.PASS;

        double dx = data.getDeltaX();
        double dz = data.getDeltaZ();
        double dh = Math.sqrt(dx * dx + dz * dz);

        if (dh > maxIceSpeed) {
            iceSpeedCount++;
            if (iceSpeedCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            iceSpeedCount = Math.max(0, iceSpeedCount - 1);
        }
        return CheckResult.PASS;
    }
}
