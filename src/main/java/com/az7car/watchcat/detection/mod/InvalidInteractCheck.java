package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

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
        if (getActionObj(interact) == null) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    private static Object getActionObj(ServerboundInteractPacket interact) {
        try {
            Field field = ServerboundInteractPacket.class.getDeclaredField("action");
            field.setAccessible(true);
            return field.get(interact);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        int entityId = interact.getEntityId();
        if (entityId < -1 || entityId == 0) {
            return CheckResult.FLAG;
        }
        if (getActionObj(interact) == null) {
            return CheckResult.FLAG;
        }
        Level level = nmsPlayer.level();
        net.minecraft.world.entity.Entity target = level.getEntity(entityId);
        if (target == null && entityId != -1) {
            return CheckResult.FLAG;
        }
        return CheckResult.PASS;
    }
}
