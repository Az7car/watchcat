package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AntiBlindCheck extends AbstractCheck {

    private int antiBlindCount;

    public AntiBlindCheck(WatchcatConfig config) {
        super("AntiBlind", "mod",
            config.getCheckWeight("mod.antiblind", 0.4),
            config.isCheckEnabled("mod.antiblind", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        boolean hasBlindness = player.hasPotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
        boolean hasDarkness = player.hasPotionEffect(org.bukkit.potion.PotionEffectType.DARKNESS);
        if (hasBlindness || hasDarkness) {
            float pitch = data.getPitch();
            float yaw = data.getYaw();
            float dp = data.getDeltaPitch();
            if (Math.abs(dp) > 3.0f) {
                antiBlindCount++;
                if (antiBlindCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                antiBlindCount = Math.max(0, antiBlindCount - 1);
            }
        }
        return CheckResult.PASS;
    }
}
