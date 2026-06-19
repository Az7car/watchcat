package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FastEatCheck extends AbstractCheck {

    private final long minEatTime;
    private long eatStartTime;

    public FastEatCheck(WatchcatConfig config) {
        super("FastEat", "combat",
            config.getCheckWeight("combat.fasteat", 0.5),
            config.isCheckEnabled("combat.fasteat", true));
        this.minEatTime = (long) config.getCheckDouble("combat.fasteat", "min-eat-time", 1000);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundUseItemPacket) {
            Material hand = player.getInventory().getItemInMainHand().getType();
            boolean isFood = hand.isEdible() || hand == Material.POTION || hand == Material.MILK_BUCKET;
            if (isFood) {
                if (eatStartTime > 0) {
                    long elapsed = System.currentTimeMillis() - eatStartTime;
                    if (elapsed < minEatTime / 2) {
                        return CheckResult.CANCELLED;
                    }
                }
                eatStartTime = System.currentTimeMillis();
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundUseItemPacket) {
            Material hand = player.getInventory().getItemInMainHand().getType();
            boolean isFood = hand.isEdible() || hand == Material.POTION || hand == Material.MILK_BUCKET;
            if (isFood) {
                long elapsed = System.currentTimeMillis() - eatStartTime;
                if (eatStartTime > 0 && elapsed < minEatTime) {
                    return CheckResult.FLAG;
                }
                eatStartTime = System.currentTimeMillis();
            }
        }
        return CheckResult.PASS;
    }
}
