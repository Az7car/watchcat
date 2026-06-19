package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import java.util.Set;

public class AntiAntiXrayCheck extends AbstractCheck {

    private static final Set<Material> SUSPICIOUS_TARGETS = Set.of(
        Material.STONE, Material.DEEPSLATE, Material.NETHERRACK,
        Material.END_STONE, Material.TUFF, Material.GRAVEL, Material.DIRT
    );

    private int suspiciousBreakCount;
    private int totalBreakCount;

    public AntiAntiXrayCheck(WatchcatConfig config) {
        super("AntiAntiXray", "mod",
            config.getCheckWeight("mod.antiantixray", 0.6),
            config.isCheckEnabled("mod.antiantixray", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return CheckResult.PASS;

        int bx = action.getPos().getX();
        int by = action.getPos().getY();
        int bz = action.getPos().getZ();
        Block block = player.getWorld().getBlockAt(bx, by, bz);

        if (SUSPICIOUS_TARGETS.contains(block.getType())) {
            totalBreakCount++;
            int nearbyOres = 0;
            for (int ox = -2; ox <= 2; ox++) {
                for (int oy = -2; oy <= 2; oy++) {
                    for (int oz = -2; oz <= 2; oz++) {
                        Block neighbor = player.getWorld().getBlockAt(bx + ox, by + oy, bz + oz);
                        if (isValuable(neighbor.getType())) {
                            nearbyOres++;
                        }
                    }
                }
            }
            if (nearbyOres > 0 && totalBreakCount < 50) {
                suspiciousBreakCount++;
                if (suspiciousBreakCount > 5) {
                    suspiciousBreakCount = 0;
                    totalBreakCount = 0;
                    return CheckResult.FLAG;
                }
            }
        }
        return CheckResult.PASS;
    }

    private boolean isValuable(Material type) {
        return type.name().contains("DIAMOND") || type.name().contains("EMERALD") ||
               type.name().contains("GOLD") || type.name().contains("NETHERITE") ||
               type.name().contains("ANCIENT") || type.name().contains("REDSTONE") ||
               type.name().contains("LAPIS");
    }
}
