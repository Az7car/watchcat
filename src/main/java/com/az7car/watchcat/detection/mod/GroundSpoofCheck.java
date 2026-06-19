package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class GroundSpoofCheck extends AbstractCheck {

    private int spoofCount;
    private boolean lastOnGround;

    public GroundSpoofCheck(WatchcatConfig config) {
        super("GroundSpoof", "mod",
            config.getCheckWeight("mod.groundspoof", 0.6),
            config.isCheckEnabled("mod.groundspoof", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        boolean onGround = move.isOnGround();

        if (onGround == lastOnGround) {
            spoofCount++;
            if (spoofCount > 20) {
                spoofCount = 0;
                return CheckResult.FLAG;
            }
        } else {
            spoofCount = Math.max(0, spoofCount - 2);
        }
        lastOnGround = onGround;
        return CheckResult.PASS;
    }
}
