package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.Material;

public class NoPotionLabelCheck extends AbstractCheck {

    private int noPotionLabelCount;

    public NoPotionLabelCheck(WatchcatConfig config) {
        super("NoPotionLabel", "mod",
            config.getCheckWeight("mod.nopotionlabel", 0.25),
            config.isCheckEnabled("mod.nopotionlabel", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        var effects = player.getActivePotionEffects();
        boolean hasEffects = !effects.isEmpty();
        if (hasEffects) {
            boolean hasPotionItems = player.getInventory().all(Material.POTION).size() > 0
                || player.getInventory().all(Material.LINGERING_POTION).size() > 0
                || player.getInventory().all(Material.SPLASH_POTION).size() > 0;
            if (hasPotionItems) {
                noPotionLabelCount++;
                if (noPotionLabelCount > 5) {
                    return CheckResult.FLAG;
                }
            }
        }
        if (noPotionLabelCount > 0 && !hasEffects) {
            noPotionLabelCount = Math.max(0, noPotionLabelCount - 1);
        }
        return CheckResult.PASS;
    }
}
