package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class PacketManiCheck extends AbstractCheck {

    private int duplicateCount;
    private long lastPacketHash;

    public PacketManiCheck(WatchcatConfig config) {
        super("PacketMani", "mod",
            config.getCheckWeight("mod.packetmani", 0.5),
            config.isCheckEnabled("mod.packetmani", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        long hash = packet.hashCode();
        if (hash == lastPacketHash) {
            duplicateCount++;
            if (duplicateCount > 10) {
                return CheckResult.FLAG;
            }
        } else {
            duplicateCount = Math.max(0, duplicateCount - 1);
        }
        lastPacketHash = hash;
        return CheckResult.PASS;
    }
}
