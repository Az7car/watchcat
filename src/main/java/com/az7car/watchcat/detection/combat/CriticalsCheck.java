package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class CriticalsCheck extends AbstractCheck {

    private final int minAirTicks;

    public CriticalsCheck(WatchcatConfig config) {
        super("Criticals", "combat",
            config.getCheckWeight("combat.criticals"),
            config.isCheckEnabled("combat.criticals"));
        this.minAirTicks = (int) config.getCheckDouble("combat.criticals", "min-air-ticks", 3);
    }

    private static boolean isAttackAction(ServerboundInteractPacket interact) {
        try {
            Field field = ServerboundInteractPacket.class.getDeclaredField("action");
            field.setAccessible(true);
            Object action = field.get(interact);
            return action != null && action.toString().equals("ATTACK");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        if (!isAttackAction(interact)) return CheckResult.PASS;
        if (!data.isOnGround() && data.getAirTicks() > 0 && data.getAirTicks() < minAirTicks) {
            if (data.wasOnGround()) {
                return CheckResult.CANCELLED;
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        if (!isAttackAction(interact)) return CheckResult.PASS;

        if (!data.isOnGround()) {
            if (data.getAirTicks() > 0 && data.getAirTicks() < minAirTicks) {
                if (data.wasOnGround()) {
                    return CheckResult.FLAG;
                }
            }
        }

        return CheckResult.PASS;
    }
}
