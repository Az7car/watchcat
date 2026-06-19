package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AntiWeaknessCheck extends AbstractCheck {

    private int antiWeaknessCount;

    public AntiWeaknessCheck(WatchcatConfig config) {
        super("AntiWeakness", "combat",
            config.getCheckWeight("combat.antiweakness", 0.5),
            config.isCheckEnabled("combat.antiweakness", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.WEAKNESS)) {
            double dmg = player.getLastDamage();
            if (dmg > 4.0) {
                antiWeaknessCount++;
                if (antiWeaknessCount > 2) {
                    return CheckResult.FLAG;
                }
            } else {
                antiWeaknessCount = Math.max(0, antiWeaknessCount - 1);
            }
        }
        return CheckResult.PASS;
    }
}
