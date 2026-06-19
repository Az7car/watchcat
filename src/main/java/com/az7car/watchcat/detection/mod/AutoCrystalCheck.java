package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class AutoCrystalCheck extends AbstractCheck {

    private long lastCrystalPlace;
    private long lastCrystalBreak;
    private int perfectCycleCount;

    public AutoCrystalCheck(WatchcatConfig config) {
        super("AutoCrystal", "mod",
            config.getCheckWeight("mod.autocrystal", 0.75),
            config.isCheckEnabled("mod.autocrystal", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundInteractPacket interact) {
            Entity target = null;
            try {
                int id = interact.getEntityId();
                target = player.getWorld().getEntities().stream()
                    .filter(e -> e.getEntityId() == id)
                    .findFirst().orElse(null);
            } catch (Exception e) {}

            if (target != null && target.getType() == EntityType.ENDER_CRYSTAL) {
                long now = System.currentTimeMillis();
                if (lastCrystalBreak > 0 && lastCrystalPlace > 0) {
                    long placeToBreak = now - lastCrystalPlace;
                    long breakToPlace = lastCrystalBreak > lastCrystalPlace ? now - lastCrystalBreak : 0;
                    if (placeToBreak < 100 && breakToPlace < 100) {
                        perfectCycleCount++;
                        if (perfectCycleCount > 5) {
                            return CheckResult.FLAG;
                        }
                    } else {
                        perfectCycleCount = Math.max(0, perfectCycleCount - 1);
                    }
                }
                lastCrystalBreak = now;
            }
        }
        return CheckResult.PASS;
    }

    public void recordCrystalPlace() {
        lastCrystalPlace = System.currentTimeMillis();
    }
}
