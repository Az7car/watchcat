package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class PredictionCheck extends AbstractCheck {

    private double predictedX, predictedZ;
    private int predictionFailCount;

    public PredictionCheck(WatchcatConfig config) {
        super("Prediction", "movement",
            config.getCheckWeight("movement.prediction", 0.65),
            config.isCheckEnabled("movement.prediction", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;

        double x = move instanceof ServerboundMovePlayerPacket.Pos ?
            ((ServerboundMovePlayerPacket.Pos) move).getX(0) :
            move instanceof ServerboundMovePlayerPacket.PosRot ?
            ((ServerboundMovePlayerPacket.PosRot) move).getX(0) : data.getLastX();

        double z = move instanceof ServerboundMovePlayerPacket.Pos ?
            ((ServerboundMovePlayerPacket.Pos) move).getZ(0) :
            move instanceof ServerboundMovePlayerPacket.PosRot ?
            ((ServerboundMovePlayerPacket.PosRot) move).getZ(0) : data.getLastZ();

        if (predictionFailCount > 0) {
            double dx = x - predictedX;
            double dz = z - predictedZ;
            double error = Math.sqrt(dx * dx + dz * dz);

            if (error > 2.0) {
                predictionFailCount++;
                if (predictionFailCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                predictionFailCount = Math.max(0, predictionFailCount - 1);
            }
        }

        double dx = data.getPositionDelta().getX();
        double dz = data.getPositionDelta().getZ();
        predictedX = x + dx;
        predictedZ = z + dz;
        return CheckResult.PASS;
    }
}
