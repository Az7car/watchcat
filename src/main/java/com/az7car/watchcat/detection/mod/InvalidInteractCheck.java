package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class InvalidInteractCheck extends AbstractCheck {

    public InvalidInteractCheck(WatchcatConfig config) {
        super("InvalidInteract", "mod",
            config.getCheckWeight("mod.invalidinteract", 0.5),
            config.isCheckEnabled("mod.invalidinteract", true));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        int entityId = interact.getEntityId();
        if (entityId < -1 || entityId == 0) {
            return CheckResult.CANCELLED;
        }
        if (interact.getAction() == null) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        int entityId = interact.getEntityId();
        if (entityId < -1 || entityId == 0) {
            return CheckResult.FLAG;
        }
        if (interact.getAction() == null) {
            return CheckResult.FLAG;
        }
        net.minecraft.world.entity.Entity target = nmsPlayer.serverLevel().getEntity(entityId);
        if (target == null && entityId != -1) {
            return CheckResult.FLAG;
        }
        return CheckResult.PASS;
    }
}
