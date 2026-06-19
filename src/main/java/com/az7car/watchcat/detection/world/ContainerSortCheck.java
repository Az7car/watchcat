package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ContainerSortCheck extends AbstractCheck {

    private int containerSortCount;
    private int lastSlot;

    public ContainerSortCheck(WatchcatConfig config) {
        super("ContainerSort", "world",
            config.getCheckWeight("world.containersort", 0.35),
            config.isCheckEnabled("world.containersort", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundContainerClickPacket click)) return CheckResult.PASS;
        int slot = click.slotNum();
        if (lastSlot != 0 && slot != lastSlot) {
            long timeDiff = Math.abs(System.nanoTime() - data.getLastPacketTime());
            if (timeDiff < 1_000_000) {
                containerSortCount++;
                if (containerSortCount > 10) {
                    return CheckResult.FLAG;
                }
            } else {
                containerSortCount = Math.max(0, containerSortCount - 1);
            }
        }
        lastSlot = slot;
        return CheckResult.PASS;
    }
}
