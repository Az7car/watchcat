package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.util.RotationUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class KillauraCheck extends AbstractCheck {

    private final double gcdThreshold;
    private final double cinematicR2Threshold;
    private final double constellationThreshold;

    public KillauraCheck(WatchcatConfig config) {
        super("Killaura", "combat",
            config.getCheckWeight("combat.killaura"),
            config.isCheckEnabled("combat.killaura"));
        this.gcdThreshold = config.getCheckDouble("combat.killaura", "gcd-threshold", 0.008);
        this.cinematicR2Threshold = config.getCheckDouble("combat.killaura", "cinematic-r2-threshold", 0.92);
        this.constellationThreshold = config.getCheckDouble("combat.killaura", "constellation-threshold", 0.75);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        data.recordAttack();
        float gcd = data.computeGcd();
        if (gcd > 0 && gcd < gcdThreshold * 0.5) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        data.recordAttack();

        float gcd = data.computeGcd();
        double confidence = 0.0;

        if (gcd > 0 && gcd < gcdThreshold) {
            confidence += 0.35;
        }

        double rSquared = RotationUtils.cinematicFit(data.getRotationDeltas());
        if (rSquared > cinematicR2Threshold) {
            confidence += 0.30;
        }

        float gcdPitch = RotationUtils.gcdDelta(Math.abs(data.getDeltaPitch()), Math.abs(data.getLastDeltaPitch()));
        if (gcdPitch > 0 && gcdPitch < gcdThreshold) {
            confidence += 0.25;
        }

        var rotations = data.getRotationBuffer();
        if (rotations.size() >= 10) {
            double[] yawDeltas = RotationUtils.getDeltaYawArray(rotations);
            double yawStd = MathUtils.stdDev(yawDeltas);
            if (yawStd < 0.3 && yawStd > 0.001) {
                confidence += 0.15;
            }
        }

        return confidence > constellationThreshold ? CheckResult.FLAG : CheckResult.PASS;
    }
}
