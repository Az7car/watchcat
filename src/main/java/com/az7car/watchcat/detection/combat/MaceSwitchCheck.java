package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MaceSwitchCheck extends AbstractCheck {

    private final double requiredFallDistance;
    private final long switchWindow;
    private double playerFallDistance;
    private int validSwitches;

    public MaceSwitchCheck(WatchcatConfig config) {
        super("MaceSwitch", "combat",
            config.getCheckWeight("combat.maceswitch", 0.7),
            config.isCheckEnabled("combat.maceswitch", true));
        this.requiredFallDistance = config.getCheckDouble("combat.maceswitch", "required-fall-distance", 1.5);
        this.switchWindow = (long) config.getCheckDouble("combat.maceswitch", "switch-window", 100);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundPlayerCommandPacket cmd) {
            if (cmd.getAction() == ServerboundPlayerCommandPacket.Action.START_USE_ITEM) {
                return CheckResult.PASS;
            }
        }
        if (!(packet instanceof net.minecraft.network.protocol.game.ServerboundInteractPacket)) return CheckResult.PASS;

        Material hand = player.getInventory().getItemInMainHand().getType();
        if (hand != Material.MACE) return CheckResult.PASS;

        float fallDist = player.getFallDistance();
        if (fallDist < requiredFallDistance) return CheckResult.PASS;

        long now = System.currentTimeMillis();
        Long lastFall = data.getClickTimestamps().peekLast();

        if (data.getAirTicks() > 3 && data.getDeltaY() < -0.5) {
            validSwitches++;
            if (validSwitches > 3) {
                return CheckResult.CANCELLED;
            }
        }

        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof net.minecraft.network.protocol.game.ServerboundInteractPacket)) return CheckResult.PASS;

        Material hand = player.getInventory().getItemInMainHand().getType();
        if (hand != Material.MACE) return CheckResult.PASS;

        float fallDist = player.getFallDistance();
        if (fallDist < requiredFallDistance) return CheckResult.PASS;

        if (data.getAirTicks() > 3 && data.getDeltaY() < -0.5) {
            if (player.getTicksLived() > 100) {
                validSwitches++;
                if (validSwitches > 2) {
                    return CheckResult.FLAG;
                }
            }
        }

        return CheckResult.PASS;
    }
}
