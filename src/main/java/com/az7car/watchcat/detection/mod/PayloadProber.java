package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.util.PacketUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PayloadProber extends AbstractCheck {

    private final int probeIntervalTicks;

    public PayloadProber(WatchcatConfig config) {
        super("PayloadProber", "mod",
            config.getCheckWeight("mod.payload"),
            config.isCheckEnabled("mod.payload"));
        this.probeIntervalTicks = (int) config.getCheckDouble("mod.payload", "probe-interval-ticks", 200);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (data.isProbed()) return CheckResult.PASS;
        if (player.getTicksLived() < probeIntervalTicks) return CheckResult.PASS;

        String brand = data.getClientBrand();
        if (brand == null) return CheckResult.PASS;

        if (brand.contains("vanilla") || brand.equals("")) {
            PacketUtils.sendPayload(player, "minecraft:register",
                new byte[]{0, 0, 0, 0, 0});
            data.setProbed(true);
        }

        return CheckResult.PASS;
    }
}
