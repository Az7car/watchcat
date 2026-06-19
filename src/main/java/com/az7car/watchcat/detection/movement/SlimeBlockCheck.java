package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SlimeBlockCheck extends AbstractCheck {

    private final double maxSlimeBounce;
    private int slimeCount;

    public SlimeBlockCheck(WatchcatConfig config) {
        super("SlimeBlock", "movement",
            config.getCheckWeight("movement.slimeblock", 0.5),
            config.isCheckEnabled("movement.slimeblock", true));
        this.maxSlimeBounce = config.getCheckDouble("movement.slimeblock", "max-bounce", 0.6);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        Material below = player.getLocation().subtract(0, 0.1, 0).getBlock().getType();
        if (below != Material.SLIME_BLOCK) return CheckResult.PASS;

        double dy = data.getPositionDelta().getY();
        if (dy > 0) return CheckResult.PASS;

        double bounce = Math.abs(dy);
        if (bounce < 0.01) return CheckResult.PASS;

        if (bounce > maxSlimeBounce) {
            slimeCount++;
            if (slimeCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            slimeCount = Math.max(0, slimeCount - 1);
        }
        return CheckResult.PASS;
    }
}
