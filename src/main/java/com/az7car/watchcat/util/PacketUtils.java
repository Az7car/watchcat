package com.az7car.watchcat.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.Identifier;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class PacketUtils {

    private PacketUtils() {}

    public static void sendPayload(Player player, String channel, byte[] data) {
        Identifier key = Identifier.tryParse(channel);
        if (key == null) return;
        DiscardedPayload payload = new DiscardedPayload(key, data);
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);
        ((CraftPlayer) player).getHandle().connection.connection.send(packet);
    }

    public static void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().connection.connection.send(packet);
    }

    public static String getPayloadChannel(Object packet) {
        if (packet instanceof ServerboundCustomPayloadPacket p) {
            try {
                return p.payload().type().id().toString();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static byte[] getPayloadData(Object packet) {
        if (packet instanceof ServerboundCustomPayloadPacket p) {
            try {
                Object pl = p.payload();
                if (pl instanceof DiscardedPayload dp) {
                    return dp.data();
                }
                ByteBuf buf = Unpooled.buffer();
                var type = pl.getClass().getMethod("type").invoke(pl);
                if (type != null) {
                    var id = type.getClass().getMethod("id").invoke(type);
                }
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
