package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class HeadRollCheck extends AbstractCheck {

    private final double maxPitchChange;
    private long lastWarning;

    public HeadRollCheck(WatchcatConfig config) {
        super("HeadRoll", "mod",
            config.getCheckWeight("mod.headroll", 0.5),
            config.isCheckEnabled("mod.headroll", true));
        this.maxPitchChange = config.getCheckDouble("mod.headroll", "max-pitch-change", 5.0);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket.Rot
            || packet instanceof ServerboundMovePlayerPacket.PosRot)) return CheckResult.PASS;
        float dp = Math.abs(data.getDeltaPitch());
        float dy = Math.abs(data.getDeltaYaw());
        if (dp > maxPitchChange * 3 && dy > maxPitchChange * 10) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket.Rot
            || packet instanceof ServerboundMovePlayerPacket.PosRot)) return CheckResult.PASS;
        float dp = Math.abs(data.getDeltaPitch());
        float dy = Math.abs(data.getDeltaYaw());
        if (dp > maxPitchChange && dy > maxPitchChange * 5) {
            if (System.currentTimeMillis() - lastWarning > 5000) {
                lastWarning = System.currentTimeMillis();
                return CheckResult.FLAG;
            }
        }
        return CheckResult.PASS;
    }
}
