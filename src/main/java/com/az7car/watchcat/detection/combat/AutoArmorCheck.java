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

public class AutoArmorCheck extends AbstractCheck {

    private final long minEquipTime;
    private Long lastEquipTime;

    public AutoArmorCheck(WatchcatConfig config) {
        super("AutoArmor", "combat",
            config.getCheckWeight("combat.autoarmor", 0.5),
            config.isCheckEnabled("combat.autoarmor", true));
        this.minEquipTime = (long) config.getCheckDouble("combat.autoarmor", "min-equip-time", 50);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundContainerClickPacket click)) return CheckResult.PASS;
        ItemStack carried = click.getCarriedItem() != null
            ? ItemStack.deserializeBytes(click.getCarriedItem().getValue()) : null;
        if (carried == null) return CheckResult.PASS;
        Material type = carried.getType();
        boolean isArmor = type.name().endsWith("_HELMET") || type.name().endsWith("_CHESTPLATE")
            || type.name().endsWith("_LEGGINGS") || type.name().endsWith("_BOOTS");
        if (!isArmor) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        if (lastEquipTime != null) {
            long interval = now - lastEquipTime;
            if (interval < minEquipTime) {
                return CheckResult.FLAG;
            }
        }
        lastEquipTime = now;
        return CheckResult.PASS;
    }
}
