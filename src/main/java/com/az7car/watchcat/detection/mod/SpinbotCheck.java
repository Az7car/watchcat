package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.util.MathUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class SpinbotCheck extends AbstractCheck {

    private final double maxYawDelta;
    private final int minSamples;

    public SpinbotCheck(WatchcatConfig config) {
        super("Spinbot", "mod",
            config.getCheckWeight("mod.spinbot", 0.6),
            config.isCheckEnabled("mod.spinbot", true));
        this.maxYawDelta = config.getCheckDouble("mod.spinbot", "max-yaw-delta", 180);
        this.minSamples = (int) config.getCheckDouble("mod.spinbot", "min-samples", 5);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket.Rot
            || packet instanceof ServerboundMovePlayerPacket.PosRot)) return CheckResult.PASS;
        float dy = Math.abs(data.getDeltaYaw());
        if (dy > maxYawDelta) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket.Rot
            || packet instanceof ServerboundMovePlayerPacket.PosRot)) return CheckResult.PASS;
        var deltas = data.getRotationDeltas();
        if (deltas.size() < minSamples) return CheckResult.PASS;
        double[] yawDeltas = deltas.stream().mapToDouble(d -> (double)d[0]).toArray();
        double mean = MathUtils.mean(yawDeltas);
        double stdDev = MathUtils.stdDev(yawDeltas);
        if (mean > maxYawDelta * 0.5 && stdDev < mean * 0.1) {
            return CheckResult.FLAG;
        }
        return CheckResult.PASS;
    }
}
