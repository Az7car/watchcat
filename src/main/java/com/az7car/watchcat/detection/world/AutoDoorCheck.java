package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.Material;

public class AutoDoorCheck extends AbstractCheck {

    private int autoDoorCount;

    public AutoDoorCheck(WatchcatConfig config) {
        super("AutoDoor", "world",
            config.getCheckWeight("world.autodoor", 0.35),
            config.isCheckEnabled("world.autodoor", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            var block = player.getTargetBlockExact(5);
            if (block != null) {
                Material type = block.getBlockData().getMaterial();
                if (type.name().contains("DOOR") || type.name().contains("GATE")) {
                    autoDoorCount++;
                    if (autoDoorCount > 5) {
                        return CheckResult.FLAG;
                    }
                } else {
                    autoDoorCount = Math.max(0, autoDoorCount - 1);
                }
            }
        }
        return CheckResult.PASS;
    }
}
