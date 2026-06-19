package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AirPlaceCheck extends AbstractCheck {

    public AirPlaceCheck(WatchcatConfig config) {
        super("AirPlace", "world",
            config.getCheckWeight("world.airplace"),
            config.isCheckEnabled("world.airplace"));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundUseItemOnPacket placePacket)) return CheckResult.PASS;
        var block = player.getWorld().getBlockAt(
            placePacket.getBlockPos().getX(),
            placePacket.getBlockPos().getY(),
            placePacket.getBlockPos().getZ());
        var relative = player.getWorld().getBlockAt(
            placePacket.getBlockPos().getX() + placePacket.getDirection().getStepX(),
            placePacket.getBlockPos().getY() + placePacket.getDirection().getStepY(),
            placePacket.getBlockPos().getZ() + placePacket.getDirection().getStepZ());
        if (block.isEmpty() && relative.isEmpty()) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundUseItemOnPacket placePacket)) return CheckResult.PASS;

        Vector blockPos = new Vector(
            placePacket.getBlockPos().getX(),
            placePacket.getBlockPos().getY(),
            placePacket.getBlockPos().getZ()
        );

        Vector faceDir = new Vector(
            placePacket.getHitVector().x,
            placePacket.getHitVector().y,
            placePacket.getHitVector().z
        );

        Vector adjacentBlock = blockPos.add(faceDir);
        var world = player.getWorld();
        var adjType = world.getBlockAt(
            (int)adjacentBlock.getX(),
            (int)adjacentBlock.getY(),
            (int)adjacentBlock.getZ()
        ).getType();

        if (adjType == org.bukkit.Material.AIR) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
