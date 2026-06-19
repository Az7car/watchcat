package com.az7car.watchcat.core.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

public class PacketAccessors {

    private static final ConcurrentHashMap<Class<?>, MethodHandle> HANDLE_CACHE = new ConcurrentHashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static MethodHandle getter(Class<?> owner, String name, Class<?> type) {
        String key = owner.getName() + "." + name;
        return HANDLE_CACHE.computeIfAbsent(owner, k -> {
            try {
                Field f = findField(owner, name, type);
                if (f == null) return null;
                f.setAccessible(true);
                return LOOKUP.unreflectGetter(f);
            } catch (Exception e) {
                return null;
            }
        });
    }

    private static Field findField(Class<?> clazz, String name, Class<?> type) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getName().equals(name) && f.getType().isAssignableFrom(type)) {
                return f;
            }
        }
        Class<?> sup = clazz.getSuperclass();
        if (sup != null) return findField(sup, name, type);
        return null;
    }

    public static Channel getChannel(Player player) {
        try {
            CraftPlayer craft = (CraftPlayer) player;
            ServerGamePacketListenerImpl connection = craft.getHandle().connection;
            MethodHandle connGetter = getter(ServerGamePacketListenerImpl.class, "connection", Object.class);
            if (connGetter == null) return null;
            Object networkManager = connGetter.invoke(connection);
            MethodHandle channelGetter = getter(networkManager.getClass(), "channel", Channel.class);
            if (channelGetter == null) return null;
            return (Channel) channelGetter.invoke(networkManager);
        } catch (Throwable t) {
            return null;
        }
    }

    public static ChannelPipeline getPipeline(Player player) {
        Channel ch = getChannel(player);
        return ch != null ? ch.pipeline() : null;
    }
}
