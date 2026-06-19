package com.az7car.watchcat.detection.movement;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class DolphinJumpCheck extends AbstractCheck {

    private int dolphinJumpCount;
    private int waterTicks;

    public DolphinJumpCheck(WatchcatConfig config) {
        super("DolphinJump", "movement",
            config.getCheckWeight("movement.dolphinjump", 0.5),
            config.isCheckEnabled("movement.dolphinjump", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) return CheckResult.PASS;
        boolean inWater = player.getLocation().getBlock().getType().name().contains("WATER");
        double dy = data.getDeltaY();

        if (inWater) waterTicks++;
        else waterTicks = 0;

        if (inWater && dy > 0.3 && waterTicks > 3) {
            dolphinJumpCount++;
            if (dolphinJumpCount > 5) {
                return CheckResult.FLAG;
            }
        } else {
            dolphinJumpCount = Math.max(0, dolphinJumpCount - 1);
        }
        return CheckResult.PASS;
    }
}
