package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class RotationPlaceCheck extends AbstractCheck {

    private final float minPitchForPlace;
    private int impossiblePlaceCount;

    public RotationPlaceCheck(WatchcatConfig config) {
        super("RotationPlace", "world",
            config.getCheckWeight("world.rotationplace", 0.5),
            config.isCheckEnabled("world.rotationplace", true));
        this.minPitchForPlace = (float) config.getCheckDouble("world.rotationplace", "min-pitch", 80.0);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlaceRecipePacket)) return CheckResult.PASS;
        float pitch = data.getLastPitch();
        if (Math.abs(pitch) > minPitchForPlace) {
            impossiblePlaceCount++;
            if (impossiblePlaceCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            impossiblePlaceCount = Math.max(0, impossiblePlaceCount - 1);
        }
        return CheckResult.PASS;
    }
}
