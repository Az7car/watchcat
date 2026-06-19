package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.Material;

public class AutoSoupCheck extends AbstractCheck {

    private int autoSoupCount;
    private int lastSoupHealth;

    public AutoSoupCheck(WatchcatConfig config) {
        super("AutoSoup", "combat",
            config.getCheckWeight("combat.autosoup", 0.4),
            config.isCheckEnabled("combat.autosoup", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        var hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() != Material.MUSHROOM_STEW) return CheckResult.PASS;
        int health = (int) player.getHealth();
        if (lastSoupHealth != 0 && health > lastSoupHealth && health - lastSoupHealth >= 7) {
            autoSoupCount++;
            if (autoSoupCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            autoSoupCount = Math.max(0, autoSoupCount - 1);
        }
        lastSoupHealth = health;
        return CheckResult.PASS;
    }
}
