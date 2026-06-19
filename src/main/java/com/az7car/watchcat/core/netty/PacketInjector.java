package com.az7car.watchcat.core.netty;

import com.az7car.watchcat.WatchcatPlugin;
import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.CheckRegistry;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class PacketInjector {

    private final WatchcatPlugin plugin;
    private final WatchcatConfig config;
    private final PacketProcessor processor;
    private final CheckRegistry registry;
    private final ConcurrentHashMap<Player, Boolean> injected = new ConcurrentHashMap<>();

    public PacketInjector(WatchcatPlugin plugin, WatchcatConfig config,
                          PacketProcessor processor, CheckRegistry registry) {
        this.plugin = plugin;
        this.config = config;
        this.processor = processor;
        this.registry = registry;
    }

    public void inject(Player player) {
        if (player == null || injected.putIfAbsent(player, true) != null) return;
        WatchcatDuplexHandler.inject(player, plugin, processor, registry);
    }

    public void uninject(Player player) {
        if (player == null) return;
        injected.remove(player);
        WatchcatDuplexHandler.uninject(player);
    }

    public void injectAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            inject(player);
        }
    }

    public void shutdown() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            uninject(player);
        }
        processor.shutdown();
    }
}
