package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.util.MathUtils;
import com.az7car.watchcat.util.RotationUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AimAssistCheck extends AbstractCheck {

    private final double r2Threshold;
    private final double verticalLockDeviation;

    public AimAssistCheck(WatchcatConfig config) {
        super("AimAssist", "combat",
            config.getCheckWeight("combat.aimassist"),
            config.isCheckEnabled("combat.aimassist"));
        this.r2Threshold = config.getCheckDouble("combat.aimassist", "r2-threshold", 0.90);
        this.verticalLockDeviation = config.getCheckDouble("combat.aimassist", "vertical-lock-deviation", 0.5);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof net.minecraft.network.protocol.game.ServerboundInteractPacket)) {
            return CheckResult.PASS;
        }
        var deltas = data.getRotationDeltas();
        if (deltas.size() < 3) return CheckResult.PASS;
        float lastDeltaPitch = data.getDeltaPitch();
        if (lastDeltaPitch < 0.01f && lastDeltaPitch > -0.01f && Math.abs(data.getDeltaYaw()) > 1) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof net.minecraft.network.protocol.game.ServerboundInteractPacket)) {
            return CheckResult.PASS;
        }

        double confidence = 0.0;
        var deltas = data.getRotationDeltas();
        if (deltas.size() < 5) return CheckResult.PASS;

        double r2Pitch = RotationUtils.rsquared(
            deltas.stream().mapToDouble(d -> (double)d[1]).toArray());
        if (r2Pitch > r2Threshold) {
            confidence += 0.4;
        }

        double pitchSD = MathUtils.stdDev(
            deltas.stream().mapToDouble(d -> (double)d[1]).toArray());
        if (pitchSD < verticalLockDeviation) {
            confidence += 0.35;
        }

        int zeroPitchDeltas = 0;
        for (var d : deltas) {
            if (Math.abs(d[1]) < 0.01f) zeroPitchDeltas++;
        }
        if ((double) zeroPitchDeltas / deltas.size() > 0.3) {
            confidence += 0.25;
        }

        return confidence > 0.5 ? CheckResult.FLAG : CheckResult.PASS;
    }
}
