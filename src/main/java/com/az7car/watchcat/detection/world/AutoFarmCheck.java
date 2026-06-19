package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.core.BlockPos;
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

    private static boolean isStartDestroyBlock(Object action) {
        if (action == null) return false;
        return action.toString().equals("START_DESTROY_BLOCK");
    }

    private static Object getAction(ServerboundPlayerActionPacket pkt) {
        try {
            var m = pkt.getClass().getMethod("getAction");
            return m.invoke(pkt);
        } catch (Exception e) {
            try {
                var f = pkt.getClass().getDeclaredField("action");
                f.setAccessible(true);
                return f.get(pkt);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private static BlockPos getBlockPos(ServerboundPlayerActionPacket pkt) {
        try {
            var m = pkt.getClass().getMethod("getBlockPos");
            return (BlockPos) m.invoke(pkt);
        } catch (Exception e) {
            try {
                var f = pkt.getClass().getDeclaredField("pos");
                f.setAccessible(true);
                return (BlockPos) f.get(pkt);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket actionPacket)) return CheckResult.PASS;
        Object action = getAction(actionPacket);
        if (!isStartDestroyBlock(action)) return CheckResult.PASS;

        BlockPos pos = getBlockPos(actionPacket);
        if (pos == null) return CheckResult.PASS;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
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
