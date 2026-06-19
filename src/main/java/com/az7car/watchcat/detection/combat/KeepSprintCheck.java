package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class KeepSprintCheck extends AbstractCheck {

    private final double maxSprintSpeed;
    private double lastAttackSpeed;

    public KeepSprintCheck(WatchcatConfig config) {
        super("KeepSprint", "combat",
            config.getCheckWeight("combat.keepsprint", 0.5),
            config.isCheckEnabled("combat.keepsprint", true));
        this.maxSprintSpeed = config.getCheckDouble("combat.keepsprint", "max-sprint-speed", 0.6);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        double horizontal = data.getHorizontalPositionDelta();
        if (player.isSprinting() && horizontal > maxSprintSpeed) {
            if (Math.abs(horizontal - lastAttackSpeed) < 0.01 && lastAttackSpeed > 0) {
                return CheckResult.FLAG;
            }
            lastAttackSpeed = horizontal;
        }
        return CheckResult.PASS;
    }
}
