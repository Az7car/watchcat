package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ChestStealerCheck extends AbstractCheck {

    private final int maxStealCps;

    public ChestStealerCheck(WatchcatConfig config) {
        super("ChestStealer", "world",
            config.getCheckWeight("world.cheststealer"),
            config.isCheckEnabled("world.cheststealer"));
        this.maxStealCps = (int) config.getCheckDouble("world.cheststealer", "max-steal-cps", 4);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundContainerClickPacket)) return CheckResult.PASS;

        data.recordClick();
        var clicks = data.getClickTimestamps();
        if (clicks.size() < 3) return CheckResult.PASS;

        long now = System.currentTimeMillis();
        long recent = clicks.stream()
            .mapToLong(t -> now - t)
            .filter(t -> t < 1000)
            .count();

        if (recent > maxStealCps) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }
}
