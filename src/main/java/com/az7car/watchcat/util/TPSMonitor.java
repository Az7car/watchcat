package com.az7car.watchcat.util;

import org.bukkit.plugin.java.JavaPlugin;

public class TPSMonitor {

    private long lastTick;
    private double tps;
    private volatile boolean running;

    public void start(JavaPlugin plugin) {
        running = true;
        lastTick = System.nanoTime();
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long now = System.nanoTime();
            long diff = now - lastTick;
            lastTick = now;
            double tickTime = diff / 1_000_000_000.0;
            tps = Math.min(20.0, 1.0 / tickTime);
        }, 1L, 1L);
    }

    public void stop() {
        running = false;
    }

    public double getTps() { return tps; }
    public double getAverageTPS() { return tps; }
    public boolean isLagging() { return tps < 18.0; }
}
