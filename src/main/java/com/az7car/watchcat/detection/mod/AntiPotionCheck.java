package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class AntiPotionCheck extends AbstractCheck {

    private int antiPotionCount;

    public AntiPotionCheck(WatchcatConfig config) {
        super("AntiPotion", "mod",
            config.getCheckWeight("mod.antipotion", 0.5),
            config.isCheckEnabled("mod.antipotion", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        boolean hasBadEffect = player.hasPotionEffect(PotionEffectType.BLINDNESS)
            || player.hasPotionEffect(PotionEffectType.WEAKNESS)
            || player.hasPotionEffect(PotionEffectType.SLOWNESS)
            || player.hasPotionEffect(PotionEffectType.MINING_FATIGUE)
            || player.hasPotionEffect(PotionEffectType.HUNGER)
            || player.hasPotionEffect(PotionEffectType.POISON)
            || player.hasPotionEffect(PotionEffectType.WITHER);
        if (!hasBadEffect) {
            antiPotionCount = Math.max(0, antiPotionCount - 1);
            return CheckResult.PASS;
        }
        float hv = (float) data.getHorizontalVelocity();
        boolean hasSpeed = player.hasPotionEffect(PotionEffectType.SPEED);
        if (hasBadEffect && !hasSpeed && hv > 0.2f) {
            antiPotionCount++;
            if (antiPotionCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            antiPotionCount = Math.max(0, antiPotionCount - 1);
        }
        return CheckResult.PASS;
    }
}
