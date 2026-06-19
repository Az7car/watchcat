package com.az7car.watchcat.detection.world;

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

public class InstantMineCheck extends AbstractCheck {

    private static final Set<Material> OBSIDIAN_TYPES = Set.of(
        Material.OBSIDIAN, Material.ENDER_CHEST, Material.ANVIL,
        Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL,
        Material.ENCHANTING_TABLE, Material.RESPAWN_ANCHOR
    );

    private final long minBreakTime;
    private long breakStartTime;
    private int previousBlockX, previousBlockY, previousBlockZ;
    private boolean mining;
    private int instantCount;

    public InstantMineCheck(WatchcatConfig config) {
        super("InstantMine", "world",
            config.getCheckWeight("world.instantmine", 0.7),
            config.isCheckEnabled("world.instantmine", true));
        this.minBreakTime = (long) config.getCheckDouble("world.instantmine", "min-break-ms", 200);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            breakStartTime = System.currentTimeMillis();
            previousBlockX = action.getPos().getX();
            previousBlockY = action.getPos().getY();
            previousBlockZ = action.getPos().getZ();
            mining = true;
        }
        if (action.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK && mining) {
            long elapsed = System.currentTimeMillis() - breakStartTime;
            if (elapsed < 50) {
                return CheckResult.CANCELLED;
            }
            mining = false;
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK && mining) {
            long elapsed = System.currentTimeMillis() - breakStartTime;
            Block block = player.getWorld().getBlockAt(previousBlockX, previousBlockY, previousBlockZ);
            if (block.getType() != Material.AIR && elapsed < minBreakTime) {
                instantCount++;
                if (instantCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                instantCount = Math.max(0, instantCount - 1);
            }
            mining = false;
        }
        return CheckResult.PASS;
    }
}
