package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AutoWeaponCheck extends AbstractCheck {

    private int weaponSwitchCount;
    private long lastAttackTime;
    private Material lastWeapon;

    public AutoWeaponCheck(WatchcatConfig config) {
        super("AutoWeapon", "combat",
            config.getCheckWeight("combat.autoweapon", 0.5),
            config.isCheckEnabled("combat.autoweapon", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        Material hand = player.getInventory().getItemInMainHand().getType();
        if (lastAttackTime > 0 && hand != lastWeapon) {
            if ((now - lastAttackTime) < 50) {
                weaponSwitchCount++;
                if (weaponSwitchCount > 10) {
                    return CheckResult.FLAG;
                }
            } else {
                weaponSwitchCount = Math.max(0, weaponSwitchCount - 1);
            }
        }
        lastAttackTime = now;
        lastWeapon = hand;
        return CheckResult.PASS;
    }
}
