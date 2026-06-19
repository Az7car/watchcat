package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AutoFarmCheck extends AbstractCheck {

    public AutoFarmCheck(WatchcatConfig config) {
        super("AutoFarm", "world",
            config.getCheckWeight("world.autofarm", 0.5),
            config.isCheckEnabled("world.autofarm", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket actionPacket)) return CheckResult.PASS;
        if (actionPacket.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return CheckResult.PASS;

        int x = actionPacket.getBlockPos().getX();
        int y = actionPacket.getBlockPos().getY();
        int z = actionPacket.getBlockPos().getZ();
        Material block = player.getWorld().getBlockAt(x, y, z).getType();

        if (isCrop(block)) {
            data.recordClick();
            var clicks = data.getClickTimestamps();
            if (clicks.size() < 3) return CheckResult.PASS;

            long now = System.currentTimeMillis();
            long[] intervals = clicks.stream()
                .mapToLong(t -> now - t)
                .filter(t -> t > 0 && t < 2000)
                .toArray();

            if (intervals.length >= 3) {
                double mean = 0;
                for (long i : intervals) mean += i;
                mean /= intervals.length;
                double variance = 0;
                for (long i : intervals) variance += Math.pow(i - mean, 2);
                variance /= intervals.length;

                if (Math.sqrt(variance) / mean < 0.05 && mean < 200) {
                    return CheckResult.FLAG;
                }
            }
        }

        return CheckResult.PASS;
    }

    private boolean isCrop(Material m) {
        return m == Material.WHEAT || m == Material.CARROTS || m == Material.POTATOES
            || m == Material.BEETROOTS || m == Material.NETHER_WART;
    }
}
