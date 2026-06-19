package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BuildReachCheck extends AbstractCheck {

    private final double maxBuildReach;

    public BuildReachCheck(WatchcatConfig config) {
        super("BuildReach", "world",
            config.getCheckWeight("world.buildreach"),
            config.isCheckEnabled("world.buildreach"));
        this.maxBuildReach = config.getCheckDouble("world.buildreach", "max-build-reach", 5.5);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundUseItemOnPacket placePacket)) return CheckResult.PASS;

        Location eye = player.getEyeLocation();
        double blockX, blockY, blockZ;
        try {
            BlockHitResult hr = (BlockHitResult) placePacket.getClass().getMethod("getHitResult").invoke(placePacket);
            blockX = hr.getBlockPos().getX();
            blockY = hr.getBlockPos().getY();
            blockZ = hr.getBlockPos().getZ();
        } catch (Exception e) { return CheckResult.PASS; }

        double distance = eye.distance(new Location(player.getWorld(), blockX + 0.5, blockY + 0.5, blockZ + 0.5));

        if (distance > maxBuildReach) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
