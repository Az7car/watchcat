package com.az7car.watchcat.core.exempt;

import com.az7car.watchcat.core.config.WatchcatConfig;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExemptHandler {

    private static final Set<String> LEGIT_CHANNELS = Set.of(
        "minecraft:brand", "minecraft:register", "minecraft:debug",
        "MC|Brand", "MC|AdvCdm", "MC|BSign", "MC|BEdit", "MC|PickItem",
        "MC|TrList", "MC|OpenSign",
        "lunarclient:pm", "badlion:modapi", "badlion:custom",
        "feather:client", "labymod3:main",
        "fml:handshake", "fml:loginwrapper",
        "fabric-screen-handler-v1", "fabric:registry"
    );

    public static void handleIncoming(ServerPlayer nmsPlayer, UUID playerId, Packet<?> packet, WatchcatConfig config) {
        if (packet instanceof ServerboundCustomPayloadPacket payload) {
            String channel;
            try { channel = payload.getName(); } catch (Exception e) { return; }
            if (channel != null && LEGIT_CHANNELS.contains(channel)) {
                ExemptionSystem.exempt(playerId, ExemptionType.ALL, 5);
            }
        }
    }

    public static void handleOutgoing(ServerPlayer nmsPlayer, UUID playerId, Packet<?> packet, WatchcatConfig config) {
        if (packet instanceof ClientboundSetHealthPacket) {
            ExemptionSystem.exemptDamage(playerId);
        }
    }
}
