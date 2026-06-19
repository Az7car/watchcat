package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class PitchLimitCheck extends AbstractCheck {

    private int pitchViolations;

    public PitchLimitCheck(WatchcatConfig config) {
        super("PitchLimit", "combat",
            config.getCheckWeight("combat.pitchlimit", 0.5),
            config.isCheckEnabled("combat.pitchlimit", true));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        float pitch = data.getLastPitch();
        if (pitch > 90 || pitch < -90) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        float pitch = data.getLastPitch();
        if (pitch > 90 || pitch < -90) {
            pitchViolations++;
            if (pitchViolations > 2) {
                return CheckResult.FLAG;
            }
        } else {
            pitchViolations = Math.max(0, pitchViolations - 1);
        }
        return CheckResult.PASS;
    }
}
