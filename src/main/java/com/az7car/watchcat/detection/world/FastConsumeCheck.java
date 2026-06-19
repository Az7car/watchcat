package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FastConsumeCheck extends AbstractCheck {

    private final long minConsumeTime;
    private long consumeStart;
    private boolean consuming;

    public FastConsumeCheck(WatchcatConfig config) {
        super("FastConsume", "world",
            config.getCheckWeight("world.fastconsume", 0.5),
            config.isCheckEnabled("world.fastconsume", true));
        this.minConsumeTime = (long) config.getCheckDouble("world.fastconsume", "min-consume-time", 1500);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundUseItemPacket) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand != null && isConsumable(hand.getType())) {
                if (consuming) {
                    long elapsed = System.currentTimeMillis() - consumeStart;
                    if (elapsed < minConsumeTime / 3) {
                        return CheckResult.CANCELLED;
                    }
                }
                consumeStart = System.currentTimeMillis();
                consuming = true;
            }
        }
        if (packet instanceof ServerboundPlayerActionPacket action) {
            if (action.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) {
                if (consuming) {
                    long elapsed = System.currentTimeMillis() - consumeStart;
                    if (elapsed < minConsumeTime / 3) {
                        return CheckResult.CANCELLED;
                    }
                }
                consuming = false;
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundUseItemPacket) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand != null && isConsumable(hand.getType())) {
                if (consuming) {
                    long elapsed = System.currentTimeMillis() - consumeStart;
                    if (elapsed < minConsumeTime) {
                        return CheckResult.FLAG;
                    }
                }
                consumeStart = System.currentTimeMillis();
                consuming = true;
            }
        }
        if (packet instanceof ServerboundPlayerActionPacket action) {
            if (action.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) {
                consuming = false;
            }
        }
        return CheckResult.PASS;
    }

    private boolean isConsumable(Material type) {
        return type.isEdible() || type == Material.POTION ||
               type == Material.MILK_BUCKET || type == Material.HONEY_BOTTLE;
    }
}
