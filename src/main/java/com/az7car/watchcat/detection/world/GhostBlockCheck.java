package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class GhostBlockCheck extends AbstractCheck {

    private int ghostBlockCount;

    public GhostBlockCheck(WatchcatConfig config) {
        super("GhostBlock", "world",
            config.getCheckWeight("world.ghostblock", 0.65),
            config.isCheckEnabled("world.ghostblock", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) return CheckResult.PASS;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return CheckResult.PASS;

        int bx = action.getPos().getX();
        int by = action.getPos().getY();
        int bz = action.getPos().getZ();
        Block block = player.getWorld().getBlockAt(bx, by, bz);

        if (block.getType() == Material.AIR) {
            ghostBlockCount++;
            if (ghostBlockCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            ghostBlockCount = Math.max(0, ghostBlockCount - 1);
        }
        return CheckResult.PASS;
    }
}
