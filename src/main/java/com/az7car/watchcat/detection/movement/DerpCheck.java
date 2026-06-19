package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class DerpCheck extends AbstractCheck {

    private final int maxDerpTicks;
    private int derpTicks;

    public DerpCheck(WatchcatConfig config) {
        super("Derp", "movement",
            config.getCheckWeight("movement.derp", 0.6),
            config.isCheckEnabled("movement.derp", true));
        this.maxDerpTicks = (int) config.getCheckDouble("movement.derp", "max-derp-ticks", 5);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket.Rot
            || packet instanceof ServerboundMovePlayerPacket.PosRot)) return CheckResult.PASS;
        float pitch = data.getPitch();
        boolean derping = pitch > 89 || pitch < -89;
        if (derping) {
            derpTicks++;
            if (derpTicks > maxDerpTicks) {
                return CheckResult.CANCELLED;
            }
        } else {
            derpTicks = 0;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket.Rot
            || packet instanceof ServerboundMovePlayerPacket.PosRot)) return CheckResult.PASS;
        float pitch = data.getPitch();
        boolean derping = pitch > 89 || pitch < -89;
        if (derping) {
            derpTicks++;
            if (derpTicks > maxDerpTicks) {
                return CheckResult.FLAG;
            }
        } else {
            derpTicks = 0;
        }
        return CheckResult.PASS;
    }
}
