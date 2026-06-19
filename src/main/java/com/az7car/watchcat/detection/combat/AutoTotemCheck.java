package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class AutoTotemCheck extends AbstractCheck {

    private final long minSwapTime;

    public AutoTotemCheck(WatchcatConfig config) {
        super("AutoTotem", "combat",
            config.getCheckWeight("combat.autototem"),
            config.isCheckEnabled("combat.autototem"));
        this.minSwapTime = (long) config.getCheckDouble("combat.autototem", "min-swap-time", 50);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundContainerClickPacket)) return CheckResult.PASS;
        if (minSwapTime <= 0) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        Long lastClick = data.getClickTimestamps().peekLast();
        if (lastClick != null && lastClick > 0) {
            long interval = now - lastClick;
            if (interval < minSwapTime / 2) {
                return CheckResult.CANCELLED;
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundContainerClickPacket)) return CheckResult.PASS;

        long now = System.currentTimeMillis();
        Long lastClick = data.getClickTimestamps().peekLast();
        if (lastClick != null && lastClick > 0) {
            long interval = now - lastClick;
            if (interval < minSwapTime) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
