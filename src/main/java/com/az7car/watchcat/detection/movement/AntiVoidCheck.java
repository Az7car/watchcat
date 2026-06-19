package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AntiVoidCheck extends AbstractCheck {

    private final double voidThreshold;
    private int voidTicks;

    public AntiVoidCheck(WatchcatConfig config) {
        super("AntiVoid", "movement",
            config.getCheckWeight("movement.antivoid", 0.7),
            config.isCheckEnabled("movement.antivoid", true));
        this.voidThreshold = config.getCheckDouble("movement.antivoid", "void-threshold", -32);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        double y = move instanceof ServerboundMovePlayerPacket.Pos pos ? pos.getY(0)
            : move instanceof ServerboundMovePlayerPacket.PosRot posRot ? posRot.getY(0)
            : data.getY();
        if (y < voidThreshold) {
            voidTicks++;
            if (voidTicks > 3) {
                double deltaY = data.getDeltaY();
                if (deltaY > 0 || Math.abs(deltaY) < 0.01) {
                    return CheckResult.CANCELLED;
                }
            }
        } else {
            voidTicks = 0;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        double y = move instanceof ServerboundMovePlayerPacket.Pos pos ? pos.getY(0)
            : move instanceof ServerboundMovePlayerPacket.PosRot posRot ? posRot.getY(0)
            : data.getY();
        if (y < voidThreshold) {
            voidTicks++;
            if (voidTicks > 5 && data.getAirTicks() > 5) {
                double deltaY = data.getDeltaY();
                if (deltaY > 0) {
                    return CheckResult.FLAG;
                }
            }
        } else {
            voidTicks = 0;
        }
        return CheckResult.PASS;
    }
}
