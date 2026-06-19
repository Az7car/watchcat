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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OreBotCheck extends AbstractCheck {

    private static final Set<Material> ORE_TARGETS = Set.of(
        Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
        Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
        Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
        Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
        Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
        Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
        Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
        Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
        Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
        Material.ANCIENT_DEBRIS
    );

    private final Map<String, Integer> oreCounts = new HashMap<>();
    private int totalMined;
    private long lastMineTime;
    private int fastChainCount;
    private final long maxChainInterval;

    public OreBotCheck(WatchcatConfig config) {
        super("OreBot", "mod",
            config.getCheckWeight("mod.orebot", 0.6),
            config.isCheckEnabled("mod.orebot", true));
        this.maxChainInterval = (long) config.getCheckDouble("mod.orebot", "max-chain-interval", 200);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return CheckResult.PASS;

        int bx = action.getPos().getX();
        int by = action.getPos().getY();
        int bz = action.getPos().getZ();
        Block block = player.getWorld().getBlockAt(bx, by, bz);

        if (ORE_TARGETS.contains(block.getType())) {
            long now = System.currentTimeMillis();
            totalMined++;

            if (lastMineTime > 0 && (now - lastMineTime) < maxChainInterval) {
                fastChainCount++;
                if (fastChainCount > 5) {
                    return CheckResult.FLAG;
                }
            } else {
                fastChainCount = Math.max(0, fastChainCount - 1);
            }
            lastMineTime = now;
        }
        return CheckResult.PASS;
    }
}
