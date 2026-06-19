package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AutoPearlCheck extends AbstractCheck {

    private long lastPearlThrow;
    private int pearlCount;
    private int fastPearlCount;

    public AutoPearlCheck(WatchcatConfig config) {
        super("AutoPearl", "combat",
            config.getCheckWeight("combat.autopearl", 0.5),
            config.isCheckEnabled("combat.autopearl", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) return CheckResult.PASS;
        Material hand = player.getInventory().getItemInMainHand().getType();
        if (hand != Material.ENDER_PEARL) return CheckResult.PASS;

        long now = System.currentTimeMillis();
        if (lastPearlThrow > 0 && (now - lastPearlThrow) < 200) {
            fastPearlCount++;
            if (fastPearlCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            fastPearlCount = Math.max(0, fastPearlCount - 1);
        }
        lastPearlThrow = now;
        return CheckResult.PASS;
    }
}
