package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class AutoToolCheck extends AbstractCheck {

    private final long maxSwitchTime;
    private long lastBreakTime;
    private int lastHeldSlot;
    private Material lastBlockType;

    private static final Map<Material, String> TOOL_TAGS = Map.ofEntries(
        Map.entry(Material.DIAMOND_PICKAXE, "pickaxe"),
        Map.entry(Material.IRON_PICKAXE, "pickaxe"),
        Map.entry(Material.STONE_PICKAXE, "pickaxe"),
        Map.entry(Material.WOODEN_PICKAXE, "pickaxe"),
        Map.entry(Material.NETHERITE_PICKAXE, "pickaxe"),
        Map.entry(Material.GOLDEN_PICKAXE, "pickaxe"),
        Map.entry(Material.DIAMOND_AXE, "axe"),
        Map.entry(Material.IRON_AXE, "axe"),
        Map.entry(Material.STONE_AXE, "axe"),
        Map.entry(Material.WOODEN_AXE, "axe"),
        Map.entry(Material.NETHERITE_AXE, "axe"),
        Map.entry(Material.GOLDEN_AXE, "axe"),
        Map.entry(Material.DIAMOND_SHOVEL, "shovel"),
        Map.entry(Material.IRON_SHOVEL, "shovel"),
        Map.entry(Material.STONE_SHOVEL, "shovel"),
        Map.entry(Material.WOODEN_SHOVEL, "shovel"),
        Map.entry(Material.NETHERITE_SHOVEL, "shovel"),
        Map.entry(Material.GOLDEN_SHOVEL, "shovel"),
        Map.entry(Material.DIAMOND_HOE, "hoe"),
        Map.entry(Material.IRON_HOE, "hoe"),
        Map.entry(Material.STONE_HOE, "hoe"),
        Map.entry(Material.WOODEN_HOE, "hoe"),
        Map.entry(Material.NETHERITE_HOE, "hoe"),
        Map.entry(Material.GOLDEN_HOE, "hoe"),
        Map.entry(Material.SHEARS, "shears"),
        Map.entry(Material.FLINT_AND_STEEL, "misc")
    );

    private static final Map<Material, String> BLOCK_TAGS = Map.ofEntries(
        Map.entry(Material.STONE, "pickaxe"), Map.entry(Material.COBBLESTONE, "pickaxe"),
        Map.entry(Material.DIORITE, "pickaxe"), Map.entry(Material.ANDESITE, "pickaxe"),
        Map.entry(Material.GRANITE, "pickaxe"), Map.entry(Material.IRON_ORE, "pickaxe"),
        Map.entry(Material.DIAMOND_ORE, "pickaxe"), Map.entry(Material.DEEPSLATE, "pickaxe"),
        Map.entry(Material.OAK_LOG, "axe"), Map.entry(Material.SPRUCE_LOG, "axe"),
        Map.entry(Material.BIRCH_LOG, "axe"), Map.entry(Material.JUNGLE_LOG, "axe"),
        Map.entry(Material.ACACIA_LOG, "axe"), Map.entry(Material.DARK_OAK_LOG, "axe"),
        Map.entry(Material.MANGROVE_LOG, "axe"), Map.entry(Material.CHERRY_LOG, "axe"),
        Map.entry(Material.DIRT, "shovel"), Map.entry(Material.GRASS_BLOCK, "shovel"),
        Map.entry(Material.SAND, "shovel"), Map.entry(Material.GRAVEL, "shovel"),
        Map.entry(Material.CLAY, "shovel"), Map.entry(Material.SNOW, "shovel"),
        Map.entry(Material.SNOW_BLOCK, "shovel")
    );

    public AutoToolCheck(WatchcatConfig config) {
        super("AutoTool", "world",
            config.getCheckWeight("world.autotool", 0.6),
            config.isCheckEnabled("world.autotool", true));
        this.maxSwitchTime = (long) config.getCheckDouble("world.autotool", "max-switch-time", 50);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundSetCarriedItemPacket held) {
            lastHeldSlot = held.getSlot();
            return CheckResult.PASS;
        }
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return CheckResult.PASS;

        var block = player.getWorld().getBlockAt(
            action.getPos().getX(), action.getPos().getY(), action.getPos().getZ());
        Material blockType = block.getType();
        String neededTool = BLOCK_TAGS.get(blockType);
        if (neededTool == null) return CheckResult.PASS;

        long now = System.currentTimeMillis();
        if (lastBreakTime > 0) {
            long sinceLast = now - lastBreakTime;
            if (sinceLast < maxSwitchTime && lastBlockType != blockType) {
                ItemStack hand = player.getInventory().getItem(lastHeldSlot);
                String handTool = TOOL_TAGS.get(hand.getType());
                if (handTool != null && handTool.equals(neededTool)) {
                    return CheckResult.CANCELLED;
                }
            }
        }

        lastBreakTime = now;
        lastBlockType = blockType;
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ServerboundSetCarriedItemPacket held) {
            lastHeldSlot = held.getSlot();
            return CheckResult.PASS;
        }
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return CheckResult.PASS;

        var block = player.getWorld().getBlockAt(
            action.getPos().getX(), action.getPos().getY(), action.getPos().getZ());
        Material blockType = block.getType();
        String neededTool = BLOCK_TAGS.get(blockType);
        if (neededTool == null) return CheckResult.PASS;

        long now = System.currentTimeMillis();
        if (lastBreakTime > 0) {
            long sinceLast = now - lastBreakTime;
            if (sinceLast < maxSwitchTime && lastBlockType != blockType) {
                ItemStack hand = player.getInventory().getItem(lastHeldSlot);
                String handTool = TOOL_TAGS.get(hand.getType());
                if (handTool != null && handTool.equals(neededTool)) {
                    return CheckResult.FLAG;
                }
            }
        }

        lastBreakTime = now;
        lastBlockType = blockType;
        return CheckResult.PASS;
    }
}
