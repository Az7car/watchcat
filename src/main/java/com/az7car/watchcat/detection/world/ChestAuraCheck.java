package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ChestAuraCheck extends AbstractCheck {

    private int chestCount;
    private long lastChestClose;
    private long lastChestOpen;

    public ChestAuraCheck(WatchcatConfig config) {
        super("ChestAura", "world",
            config.getCheckWeight("world.chestaura", 0.55),
            config.isCheckEnabled("world.chestaura", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundContainerClosePacket) {
            long now = System.currentTimeMillis();
            if (lastChestClose > 0 && (now - lastChestClose) < 100) {
                chestCount++;
                if (chestCount > 10) {
                    return CheckResult.FLAG;
                }
            } else {
                chestCount = Math.max(0, chestCount - 1);
            }
            lastChestClose = now;
        }
        return CheckResult.PASS;
    }
}
