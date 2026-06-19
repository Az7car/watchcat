package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class MultiAuraCheck extends AbstractCheck {

    private final int maxTargetsPerTick;
    private Set<Integer> targetsThisTick;

    public MultiAuraCheck(WatchcatConfig config) {
        super("MultiAura", "combat",
            config.getCheckWeight("combat.multiaura", 0.7),
            config.isCheckEnabled("combat.multiaura", true));
        this.maxTargetsPerTick = (int) config.getCheckDouble("combat.multiaura", "max-targets-per-tick", 1);
        this.targetsThisTick = new HashSet<>();
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        int entityId = interact.getEntityId();
        if (entityId < 0) return CheckResult.PASS;
        if (targetsThisTick.contains(entityId)) {
            return CheckResult.PASS;
        }
        targetsThisTick.add(entityId);
        if (targetsThisTick.size() > maxTargetsPerTick) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket interact)) return CheckResult.PASS;
        int entityId = interact.getEntityId();
        if (entityId < 0) return CheckResult.PASS;
        if (targetsThisTick.contains(entityId)) {
            return CheckResult.PASS;
        }
        targetsThisTick.add(entityId);
        if (targetsThisTick.size() > maxTargetsPerTick) {
            return CheckResult.FLAG;
        }
        return CheckResult.PASS;
    }
}
