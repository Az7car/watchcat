package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Boat;

public class BoatGlitchCheck extends AbstractCheck {

    private int boatGlitchCount;

    public BoatGlitchCheck(WatchcatConfig config) {
        super("BoatGlitch", "world",
            config.getCheckWeight("world.boatglitch", 0.5),
            config.isCheckEnabled("world.boatglitch", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!player.isInsideVehicle()) {
            boatGlitchCount = Math.max(0, boatGlitchCount - 1);
            return CheckResult.PASS;
        }
        var vehicle = player.getVehicle();
        if (!(vehicle instanceof Boat)) return CheckResult.PASS;
        double dy = data.getDeltaY();
        double hd = data.getHorizontalPositionDelta();
        if (dy > 0.5 || hd > 5.0) {
            boatGlitchCount++;
            if (boatGlitchCount > 2) {
                return CheckResult.FLAG;
            }
        } else {
            boatGlitchCount = Math.max(0, boatGlitchCount - 1);
        }
        return CheckResult.PASS;
    }
}
