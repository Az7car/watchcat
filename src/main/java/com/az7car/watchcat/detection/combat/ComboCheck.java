package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class ComboCheck extends AbstractCheck {

    private int comboCount;
    private long lastComboReset;

    public ComboCheck(WatchcatConfig config) {
        super("Combo", "combat",
            config.getCheckWeight("combat.combo", 0.6),
            config.isCheckEnabled("combat.combo", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        if ((now - lastComboReset) > 5000) {
            comboCount = 0;
            lastComboReset = now;
        }
        comboCount++;
        if (comboCount > 15) {
            return CheckResult.FLAG;
        }
        return CheckResult.PASS;
    }
}
