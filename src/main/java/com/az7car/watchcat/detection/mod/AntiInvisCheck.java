package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class AntiInvisCheck extends AbstractCheck {

    private int antiInvisCount;

    public AntiInvisCheck(WatchcatConfig config) {
        super("AntiInvis", "mod",
            config.getCheckWeight("mod.antiinvis", 0.65),
            config.isCheckEnabled("mod.antiinvis", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        float yaw = data.getLastYaw();
        float pitch = data.getLastPitch();

        for (org.bukkit.entity.Player other : player.getWorld().getPlayers()) {
            if (other.equals(player)) continue;
            if (!other.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;

            org.bukkit.util.Vector toTarget = other.getEyeLocation().toVector()
                .subtract(player.getEyeLocation().toVector()).normalize();
            double targetYaw = Math.toDegrees(Math.atan2(-toTarget.getX(), toTarget.getZ()));
            double targetPitch = Math.toDegrees(Math.asin(toTarget.getY()));

            float yawDiff = (float) Math.abs(yaw - targetYaw);
            if (yawDiff > 180) yawDiff = 360 - yawDiff;
            float pitchDiff = Math.abs(pitch - (float) targetPitch);

            if (yawDiff < 15 && pitchDiff < 15) {
                antiInvisCount++;
                if (antiInvisCount > 5) {
                    return CheckResult.FLAG;
                }
                return CheckResult.PASS;
            }
        }
        antiInvisCount = Math.max(0, antiInvisCount - 1);
        return CheckResult.PASS;
    }
}
