package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoPortalOverlayCheck extends AbstractCheck {

    private int noPortalCount;
    private long lastPortalPacket;

    public NoPortalOverlayCheck(WatchcatConfig config) {
        super("NoPortalOverlay", "mod",
            config.getCheckWeight("mod.noportaloverlay", 0.35),
            config.isCheckEnabled("mod.noportaloverlay", true));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (packet instanceof ClientboundOpenScreenPacket portal) {
            lastPortalPacket = System.currentTimeMillis();
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        boolean inPortal = player.getLocation().getBlock().getType().name().contains("PORTAL");
        if (inPortal) {
            long now = System.currentTimeMillis();
            if (now - lastPortalPacket > 500) {
                noPortalCount++;
                if (noPortalCount > 3) {
                    return CheckResult.FLAG;
                }
            }
        } else {
            noPortalCount = Math.max(0, noPortalCount - 1);
        }
        return CheckResult.PASS;
    }
}
