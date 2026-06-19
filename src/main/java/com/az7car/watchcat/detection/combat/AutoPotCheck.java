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

public class AutoPotCheck extends AbstractCheck {

    private long lastThrowTime;
    private int fastThrowCount;

    public AutoPotCheck(WatchcatConfig config) {
        super("AutoPot", "combat",
            config.getCheckWeight("combat.autopot", 0.5),
            config.isCheckEnabled("combat.autopot", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) return CheckResult.PASS;
        Material hand = player.getInventory().getItemInMainHand().getType();
        if (!hand.name().contains("POTION") && hand != Material.EXPERIENCE_BOTTLE) return CheckResult.PASS;

        long now = System.currentTimeMillis();
        if (lastThrowTime > 0) {
            long interval = now - lastThrowTime;
            if (interval < 300) {
                fastThrowCount++;
                if (fastThrowCount > 5) {
                    return CheckResult.FLAG;
                }
            } else {
                fastThrowCount = Math.max(0, fastThrowCount - 1);
            }
        }
        lastThrowTime = now;
        return CheckResult.PASS;
    }
}
