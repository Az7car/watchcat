package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AirPlaceCheck extends AbstractCheck {

    public AirPlaceCheck(WatchcatConfig config) {
        super("AirPlace", "world",
            config.getCheckWeight("world.airplace"),
            config.isCheckEnabled("world.airplace"));
    }

    private static BlockHitResult getHitResult(ServerboundUseItemOnPacket pkt) {
        try {
            return (BlockHitResult) pkt.getClass().getMethod("getHitResult").invoke(pkt);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundUseItemOnPacket placePacket)) return CheckResult.PASS;
        BlockHitResult hr = getHitResult(placePacket);
        if (hr == null) return CheckResult.PASS;
        var block = player.getWorld().getBlockAt(
            hr.getBlockPos().getX(), hr.getBlockPos().getY(), hr.getBlockPos().getZ());
        var relative = player.getWorld().getBlockAt(
            hr.getBlockPos().getX() + hr.getDirection().getStepX(),
            hr.getBlockPos().getY() + hr.getDirection().getStepY(),
            hr.getBlockPos().getZ() + hr.getDirection().getStepZ());
        if (block.isEmpty() && relative.isEmpty()) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundUseItemOnPacket placePacket)) return CheckResult.PASS;
        BlockHitResult hr = getHitResult(placePacket);
        if (hr == null) return CheckResult.PASS;

        Vector blockPos = new Vector(
            hr.getBlockPos().getX(), hr.getBlockPos().getY(), hr.getBlockPos().getZ()
        );

        Vector faceDir = new Vector(
            hr.getLocation().x, hr.getLocation().y, hr.getLocation().z
        );

        Vector adjacentBlock = blockPos.add(faceDir);
        var world = player.getWorld();
        var adjacent = world.getBlockAt(
            (int) Math.floor(adjacentBlock.getX()),
            (int) Math.floor(adjacentBlock.getY()),
            (int) Math.floor(adjacentBlock.getZ()));

        if (!adjacent.isEmpty() && !adjacent.isLiquid()) {
            return CheckResult.PASS;
        }

        if (adjacent.isEmpty() && player.getLocation().distanceSquared(adjacent.getLocation()) < maxBuildDistance()) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }

    private double maxBuildDistance() {
        return 6.0;
    }
}
