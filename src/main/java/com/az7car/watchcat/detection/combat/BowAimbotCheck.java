package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class BowAimbotCheck extends AbstractCheck {

    private final double aimLockDeviation;

    public BowAimbotCheck(WatchcatConfig config) {
        super("BowAimbot", "combat",
            config.getCheckWeight("combat.bowaimbot"),
            config.isCheckEnabled("combat.bowaimbot"));
        this.aimLockDeviation = config.getCheckDouble("combat.bowaimbot", "aim-lock-deviation", 0.1);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        if (player.getInventory().getItemInMainHand().getType() != org.bukkit.Material.BOW) return CheckResult.PASS;
        float dp = Math.abs(data.getDeltaPitch());
        float dy = Math.abs(data.getDeltaYaw());
        if (dp < 0.0005f && dy > 1) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (player.getInventory().getItemInMainHand().getType()
                != org.bukkit.Material.BOW) return CheckResult.PASS;

        double pitchSD = MathUtils.stdDev(
            data.getRotationDeltas().stream().mapToDouble(d -> (double)d[1]).toArray());
        double yawSD = MathUtils.stdDev(
            data.getRotationDeltas().stream().mapToDouble(d -> (double)d[0]).toArray());

        if (pitchSD < aimLockDeviation && yawSD < aimLockDeviation * 3) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
