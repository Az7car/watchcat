package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoHurtCamCheck extends AbstractCheck {

    private long lastHurtPacket;
    private int noHurtCount;

    public NoHurtCamCheck(WatchcatConfig config) {
        super("NoHurtCam", "combat",
            config.getCheckWeight("combat.nohurtcam", 0.45),
            config.isCheckEnabled("combat.nohurtcam", true));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ClientboundHurtAnimationPacket hurt) {
            lastHurtPacket = System.currentTimeMillis();
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        float health = data.getHealth();
        if (health < health - 4.0f && System.currentTimeMillis() - lastHurtPacket > 500) {
            noHurtCount++;
            if (noHurtCount > 2) {
                return CheckResult.FLAG;
            }
        } else {
            noHurtCount = Math.max(0, noHurtCount - 1);
        }
        return CheckResult.PASS;
    }
}
