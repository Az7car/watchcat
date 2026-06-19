package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class InvisibleAimCheck extends AbstractCheck {

    private int invisibleAimCount;

    public InvisibleAimCheck(WatchcatConfig config) {
        super("InvisibleAim", "combat",
            config.getCheckWeight("combat.invisibleaim", 0.7),
            config.isCheckEnabled("combat.invisibleaim", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        Entity target = null;
        try {
            int id = interact.getEntityId();
            target = player.getWorld().getEntities().stream()
                .filter(e -> e.getEntityId() == id)
                .findFirst().orElse(null);
        } catch (Exception e) {}

        if (target != null && target instanceof Player targetPlayer) {
            if (targetPlayer.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY)) {
                invisibleAimCount++;
                if (invisibleAimCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                invisibleAimCount = Math.max(0, invisibleAimCount - 1);
            }
        }
        return CheckResult.PASS;
    }
}
