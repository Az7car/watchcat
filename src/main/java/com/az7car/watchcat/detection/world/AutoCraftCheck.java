package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AutoCraftCheck extends AbstractCheck {

    private int autoCraftCount;
    private long lastCraftTime;

    public AutoCraftCheck(WatchcatConfig config) {
        super("AutoCraft", "world",
            config.getCheckWeight("world.autocraft", 0.35),
            config.isCheckEnabled("world.autocraft", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlaceRecipePacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        if (lastCraftTime != 0 && now - lastCraftTime < 100) {
            autoCraftCount++;
            if (autoCraftCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            autoCraftCount = Math.max(0, autoCraftCount - 1);
        }
        lastCraftTime = now;
        return CheckResult.PASS;
    }
}
