package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class LongJumpCheck extends AbstractCheck {

    private final double maxHorizontalDelta;

    public LongJumpCheck(WatchcatConfig config) {
        super("LongJump", "movement",
            config.getCheckWeight("movement.longjump"),
            config.isCheckEnabled("movement.longjump"));
        this.maxHorizontalDelta = config.getCheckDouble("movement.longjump", "max-horizontal-delta", 0.7);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.isOnGround()) return CheckResult.PASS;

        double horizontal = data.getHorizontalPositionDelta();
        if (horizontal > maxHorizontalDelta && !player.isInWater()) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
