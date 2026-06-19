package com.az7car.watchcat.core.pipeline;

import com.az7car.watchcat.core.exempt.ExemptionSystem;
import com.az7car.watchcat.core.falsepositive.CrossCheckVerifier;
import com.az7car.watchcat.core.falsepositive.FPDetector;
import com.az7car.watchcat.core.falsepositive.FPStatistics;
import com.az7car.watchcat.core.falsepositive.LagCompensation;
import com.az7car.watchcat.core.falsepositive.TrustFactor;
import com.az7car.watchcat.core.falsepositive.VerificationSystem;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class CheckExecutor {

    private final FPDetector fpDetector;
    private final TrustFactor trustFactor;
    private final LagCompensation lagComp;
    private final VerificationSystem verification;

    public CheckExecutor() {
        this.fpDetector = FPDetector.getInstance();
        this.trustFactor = TrustFactor.getInstance();
        this.lagComp = LagCompensation.getInstance();
        this.verification = VerificationSystem.getInstance();
    }

    public CheckResult execute(AbstractCheck check, Player player, PlayerData data,
                                Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!check.isEnabled()) return CheckResult.PASS;
        UUID pid = player.getUniqueId();
        if (ExemptionSystem.isCheckExempt(pid, check.getName(), check.getCategory())) return CheckResult.PASS;

        try {
            CheckResult result = check.runWithProfiling(player, data, packet, nmsPlayer);
            if (result != CheckResult.PASS) {
                FPStatistics.recordFlag(check.getName());
            }
            return applyFPProtection(check, pid, result);
        } catch (Exception e) {
            return CheckResult.PASS;
        }
    }

    public CheckResult executeSync(AbstractCheck check, Player player, PlayerData data,
                                    Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!check.isEnabled()) return CheckResult.PASS;
        UUID pid = player.getUniqueId();
        if (ExemptionSystem.isCheckExempt(pid, check.getName(), check.getCategory())) return CheckResult.PASS;

        try {
            CheckResult result = check.runSyncWithProfiling(player, data, packet, nmsPlayer);
            if (result == CheckResult.CANCELLED) return result;
            if (result != CheckResult.PASS) {
                FPStatistics.recordFlag(check.getName());
            }
            return applyFPProtection(check, pid, result);
        } catch (Exception e) {
            return CheckResult.PASS;
        }
    }

    private CheckResult applyFPProtection(AbstractCheck check, UUID playerId, CheckResult result) {
        if (result == CheckResult.PASS) return result;
        if (lagComp.shouldSkipCheck(playerId)) return CheckResult.PASS;
        if (fpDetector.isKnownFP(check.getName())) {
            if (result == CheckResult.CANCELLED) return result;
            return CheckResult.PASS;
        }
        if (fpDetector.isLikelyFalsePositive(playerId, check.getName())) {
            if (result == CheckResult.CANCELLED) return result;
            if (Math.random() > 0.3) return CheckResult.PASS;
        }
        double adjustedThreshold = trustFactor.getAdjustedThreshold(playerId, check.getWeight());
        if (adjustedThreshold > 1.0 && result == CheckResult.FLAG) return CheckResult.PASS;
        if (!CrossCheckVerifier.shouldConfirm(playerId, check.getName(), check.getCategory())) {
            if (result == CheckResult.CANCELLED) return result;
            return CheckResult.PASS;
        }
        if (verification.shouldFlag(playerId, check.getName(), result)) {
            fpDetector.recordFlag(playerId, check.getName());
            FPStatistics.recordConfirmed(check.getName());
            return result;
        }
        if (result == CheckResult.CANCELLED) return result;
        return CheckResult.PASS;
    }
}
