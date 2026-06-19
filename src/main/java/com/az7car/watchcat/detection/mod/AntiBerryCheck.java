package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.Material;

public class AntiBerryCheck extends AbstractCheck {

    private int antiBerryCount;

    public AntiBerryCheck(WatchcatConfig config) {
        super("AntiBerry", "mod",
            config.getCheckWeight("mod.antiberry", 0.4),
            config.isCheckEnabled("mod.antiberry", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        var loc = player.getLocation();
        boolean nearBerry = loc.clone().add(0, 0, 0).getBlock().getType() == Material.SWEET_BERRY_BUSH
            || loc.clone().add(0.5, 0, 0.5).getBlock().getType() == Material.SWEET_BERRY_BUSH
            || loc.clone().add(-0.5, 0, 0.5).getBlock().getType() == Material.SWEET_BERRY_BUSH
            || loc.clone().add(0.5, 0, -0.5).getBlock().getType() == Material.SWEET_BERRY_BUSH
            || loc.clone().add(-0.5, 0, -0.5).getBlock().getType() == Material.SWEET_BERRY_BUSH;
        if (nearBerry && player.getNoDamageTicks() == 0 && player.getHealth() == player.getMaxHealth()) {
            antiBerryCount++;
            if (antiBerryCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            antiBerryCount = Math.max(0, antiBerryCount - 1);
        }
        return CheckResult.PASS;
    }
}
