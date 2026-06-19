package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SuperKnockbackCheck extends AbstractCheck {

    private final double maxKnockbackVelocity;
    private int knockbackCount;

    public SuperKnockbackCheck(WatchcatConfig config) {
        super("SuperKnockback", "combat",
            config.getCheckWeight("combat.superknockback", 0.6),
            config.isCheckEnabled("combat.superknockback", true));
        this.maxKnockbackVelocity = config.getCheckDouble("combat.superknockback", "max-kb-velocity", 0.8);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        Vector velocity = player.getVelocity();
        double horizontal = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
        if (horizontal > maxKnockbackVelocity) {
            knockbackCount++;
            if (knockbackCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            knockbackCount = Math.max(0, knockbackCount - 1);
        }
        return CheckResult.PASS;
    }
}
