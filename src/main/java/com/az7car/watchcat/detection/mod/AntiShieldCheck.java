package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AntiShieldCheck extends AbstractCheck {

    private int antiShieldCount;

    public AntiShieldCheck(WatchcatConfig config) {
        super("AntiShield", "mod",
            config.getCheckWeight("mod.antishield", 0.5),
            config.isCheckEnabled("mod.antishield", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        boolean hasShield = offHand != null && offHand.getType().name().contains("SHIELD");
        if (hasShield && !data.isBlocking()) {
            antiShieldCount++;
            if (antiShieldCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            antiShieldCount = Math.max(0, antiShieldCount - 1);
        }
        return CheckResult.PASS;
    }
}
