package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AutoFishCheck extends AbstractCheck {

    private long lastFishUse;
    private int fishCount;

    public AutoFishCheck(WatchcatConfig config) {
        super("AutoFish", "world",
            config.getCheckWeight("world.autofish", 0.4),
            config.isCheckEnabled("world.autofish", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;

        if (player.getInventory().getItemInMainHand().getType() == Material.FISHING_ROD
            || player.getInventory().getItemInOffHand().getType() == Material.FISHING_ROD) {
            long now = System.currentTimeMillis();

            if (lastFishUse > 0) {
                long interval = now - lastFishUse;
                if (Math.abs(interval - 2000) < 50 || Math.abs(interval - 1000) < 50) {
                    fishCount++;
                    if (fishCount > 5) {
                        return CheckResult.FLAG;
                    }
                } else {
                    fishCount = Math.max(0, fishCount - 1);
                }
            }
            lastFishUse = now;
        }

        return CheckResult.PASS;
    }
}
