package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoPitchLimitCheck extends AbstractCheck {

    private int pitchLimitCount;

    public NoPitchLimitCheck(WatchcatConfig config) {
        super("NoPitchLimit", "mod",
            config.getCheckWeight("mod.nopitchlimit", 0.6),
            config.isCheckEnabled("mod.nopitchlimit", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        float pitch = 0;
        boolean hasRot = false;
        try {
            if (move instanceof ServerboundMovePlayerPacket.PosRot) {
                pitch = (float) ((ServerboundMovePlayerPacket.PosRot) move).getClass().getMethod("getPitch").invoke(move);
                hasRot = true;
            } else if (move instanceof ServerboundMovePlayerPacket.Rot) {
                pitch = (float) ((ServerboundMovePlayerPacket.Rot) move).getClass().getMethod("getPitch").invoke(move);
                hasRot = true;
            }
        } catch (Exception e) { return CheckResult.PASS; }
        if (!hasRot) return CheckResult.PASS;

        if (pitch > 90 || pitch < -90) {
            pitchLimitCount++;
            if (pitchLimitCount > 2) {
                return CheckResult.FLAG;
            }
        } else {
            pitchLimitCount = Math.max(0, pitchLimitCount - 1);
        }
        return CheckResult.PASS;
    }
}
