package com.az7car.watchcat.detection.combat;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class CPSLimitCheck extends AbstractCheck {

    private final int maxCPS;
    private long[] clickTimestamps;
    private int clickIndex;

    public CPSLimitCheck(WatchcatConfig config) {
        super("CPSLimit", "combat",
            config.getCheckWeight("combat.cpslimit", 0.5),
            config.isCheckEnabled("combat.cpslimit", true));
        this.maxCPS = config.getCheckInt("combat.cpslimit", "max-cps-limit", 25);
        this.clickTimestamps = new long[100];
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        clickTimestamps[clickIndex % clickTimestamps.length] = now;
        clickIndex++;
        int count = 0;
        for (long t : clickTimestamps) {
            if (t > 0 && (now - t) < 1000) count++;
        }
        if (count > maxCPS + 5) {
            return CheckResult.CANCELLED;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundInteractPacket)) return CheckResult.PASS;
        long now = System.currentTimeMillis();
        int count = 0;
        for (long t : clickTimestamps) {
            if (t > 0 && (now - t) < 1000) count++;
        }
        if (count > maxCPS) {
            return CheckResult.FLAG;
        }
        return CheckResult.PASS;
    }
}
