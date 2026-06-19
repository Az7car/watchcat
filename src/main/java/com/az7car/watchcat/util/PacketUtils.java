package com.az7car.watchcat.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class PacketUtils {

    private PacketUtils() {}

    public static void sendPayload(Player player, String channel, byte[] data) {
        ResourceLocation key = ResourceLocation.tryParse(channel);
        if (key == null) return;
        ByteBuf buf = Unpooled.wrappedBuffer(data);
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(key, buf);
        ((CraftPlayer) player).getHandle().connection.connection.send(packet);
    }

    public static void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().connection.connection.send(packet);
    }

    public static String getPayloadChannel(Object packet) {
        if (packet instanceof ServerboundCustomPayloadPacket p) {
            return p.getName();
        }
        return null;
    }

    public static byte[] getPayloadData(Object packet) {
        if (packet instanceof ServerboundCustomPayloadPacket p) {
            ByteBuf buf = p.getData();
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            buf.resetReaderIndex();
            return data;
        }
        return null;
    }
}
