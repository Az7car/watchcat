package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class AntiLevitationCheck extends AbstractCheck {

    private int levitationBypassCount;

    public AntiLevitationCheck(WatchcatConfig config) {
        super("AntiLevitation", "movement",
            config.getCheckWeight("movement.antilevitation", 0.5),
            config.isCheckEnabled("movement.antilevitation", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        if (!player.hasPotionEffect(PotionEffectType.LEVITATION)) return CheckResult.PASS;
        if (move.isOnGround()) return CheckResult.PASS;

        double dy = data.getPositionDelta().getY();
        if (dy < 0) {
            levitationBypassCount++;
            if (levitationBypassCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            levitationBypassCount = Math.max(0, levitationBypassCount - 1);
        }
        return CheckResult.PASS;
    }
}
