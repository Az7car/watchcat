package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoSlowCheck extends AbstractCheck {

    private int noSlowCount;

    public NoSlowCheck(WatchcatConfig config) {
        super("NoSlow", "combat",
            config.getCheckWeight("combat.noslow", 0.6),
            config.isCheckEnabled("combat.noslow", true));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerCommandPacket)) return CheckResult.PASS;
        double horizontal = data.getHorizontalPositionDelta();
        if (player.isHandRaised() && horizontal > 0.15) {
            noSlowCount++;
            if (noSlowCount > 5) {
                return CheckResult.CANCELLED;
            }
        } else {
            noSlowCount = 0;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerCommandPacket)) return CheckResult.PASS;
        double horizontal = data.getHorizontalPositionDelta();
        if (player.isHandRaised() && horizontal > 0.15) {
            noSlowCount++;
            if (noSlowCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            noSlowCount = 0;
        }
        return CheckResult.PASS;
    }
}
