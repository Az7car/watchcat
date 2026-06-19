package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AntiHungerCheck extends AbstractCheck {

    private int antiHungerCount;
    private int lastFoodLevel;

    public AntiHungerCheck(WatchcatConfig config) {
        super("AntiHunger", "combat",
            config.getCheckWeight("combat.antihunger", 0.4),
            config.isCheckEnabled("combat.antihunger", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        int food = player.getFoodLevel();
        if (lastFoodLevel != 0 && food > lastFoodLevel) {
            float saturation = player.getSaturation();
            if (saturation < 1.0f && (food - lastFoodLevel) > 2) {
                antiHungerCount++;
                if (antiHungerCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                antiHungerCount = Math.max(0, antiHungerCount - 1);
            }
        }
        lastFoodLevel = food;
        return CheckResult.PASS;
    }
}
