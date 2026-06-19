package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class SoundPosCheck extends AbstractCheck {

    private int soundPosCount;
    private boolean wasBehindWall;
    private double soundReactionDelta;

    public SoundPosCheck(WatchcatConfig config) {
        super("SoundPos", "mod",
            config.getCheckWeight("mod.soundpos", 0.4),
            config.isCheckEnabled("mod.soundpos", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        var nearby = player.getNearbyEntities(15, 15, 15);
        var hostile = nearby.stream()
            .filter(e -> e instanceof org.bukkit.entity.Monster)
            .findAny();
        var target = player.getTargetEntity(10);
        if (hostile.isPresent() && target == null) {
            var loc = hostile.get().getLocation();
            var blocks = new org.bukkit.util.BlockIterator(player.getWorld(),
                player.getEyeLocation().toVector(),
                loc.toVector().subtract(player.getEyeLocation().toVector()).normalize(),
                0, (int) player.getEyeLocation().distance(loc));
            int wallCount = 0;
            while (blocks.hasNext()) {
                if (blocks.next().getType().isOccluding()) wallCount++;
            }
            wasBehindWall = wallCount > 1;
            if (wasBehindWall) {
                soundReactionDelta = data.getHorizontalPositionDelta();
            }
        }
        if (wasBehindWall && soundReactionDelta > 0.3) {
            soundPosCount++;
            if (soundPosCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            soundPosCount = Math.max(0, soundPosCount - 1);
        }
        return CheckResult.PASS;
    }
}
