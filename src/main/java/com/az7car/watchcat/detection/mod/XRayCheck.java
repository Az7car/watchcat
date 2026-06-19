package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class XRayCheck extends AbstractCheck {

    private static final Set<Material> VALUABLE_ORES = Set.of(
        Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
        Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
        Material.NETHERITE_SCRAP, Material.ANCIENT_DEBRIS,
        Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE
    );

    private final Map<Material, Integer> totalMined = new HashMap<>();
    private final Map<Material, Integer> valuableMined = new HashMap<>();
    private int blocksBroken;

    public XRayCheck(WatchcatConfig config) {
        super("XRay", "mod",
            config.getCheckWeight("mod.xray", 0.7),
            config.isCheckEnabled("mod.xray", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return CheckResult.PASS;

        return CheckResult.PASS;
    }

    public void recordBlockBreak(Material type) {
        blocksBroken++;
        totalMined.merge(type, 1, Integer::sum);
        if (VALUABLE_ORES.contains(type)) {
            valuableMined.merge(type, 1, Integer::sum);
        }

        if (blocksBroken > 200) {
            double valuableRatio = getValuableRatio();
            if (valuableRatio > 0.15) {
                blocksBroken = 0;
                totalMined.clear();
                valuableMined.clear();
            }
            blocksBroken = 0;
            totalMined.clear();
            valuableMined.clear();
        }
    }

    public double getValuableRatio() {
        int totalValuable = valuableMined.values().stream().mapToInt(i -> i).sum();
        if (blocksBroken == 0) return 0;
        return (double) totalValuable / blocksBroken;
    }

    public int getBlocksBroken() { return blocksBroken; }

    public boolean hasFlagged() {
        return blocksBroken > 100 && getValuableRatio() > 0.20;
    }
}
