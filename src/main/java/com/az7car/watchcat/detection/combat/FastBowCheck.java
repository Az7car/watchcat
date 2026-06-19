package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class FastBowCheck extends AbstractCheck {

    private final long minDrawTime;
    private Long drawStartTime;

    public FastBowCheck(WatchcatConfig config) {
        super("FastBow", "combat",
            config.getCheckWeight("combat.fastbow", 0.6),
            config.isCheckEnabled("combat.fastbow", true));
        this.minDrawTime = (long) config.getCheckDouble("combat.fastbow", "min-draw-time", 300);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundUseItemPacket) {
            drawStartTime = System.currentTimeMillis();
        }
        if (packet instanceof ServerboundPlayerCommandPacket command) {
            try {
                Object action = command.getClass().getMethod("getAction").invoke(command);
                if (action != null && action.toString().equals("START_FALL_FLYING")) {
                    return CheckResult.PASS;
                }
            } catch (Exception ignored) {}
            if (drawStartTime != null && player.getInventory().getItemInMainHand().getType()
                    == org.bukkit.Material.BOW) {
                long drawTime = System.currentTimeMillis() - drawStartTime;
                if (drawTime < minDrawTime / 2) {
                    drawStartTime = null;
                    return CheckResult.CANCELLED;
                }
            }
            drawStartTime = null;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundUseItemPacket) {
            drawStartTime = System.currentTimeMillis();
        }
        if (packet instanceof ServerboundPlayerCommandPacket command) {
            try {
                Object action = command.getClass().getMethod("getAction").invoke(command);
                if (action != null && action.toString().equals("START_FALL_FLYING")) {
                    return CheckResult.PASS;
                }
            } catch (Exception ignored) {}
            if (drawStartTime != null && player.getInventory().getItemInMainHand().getType()
                    == org.bukkit.Material.BOW) {
                long drawTime = System.currentTimeMillis() - drawStartTime;
                if (drawTime < minDrawTime) {
                    drawStartTime = null;
                    return CheckResult.FLAG;
                }
            }
            drawStartTime = null;
        }
        return CheckResult.PASS;
    }
}
