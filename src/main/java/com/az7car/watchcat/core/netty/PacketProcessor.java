package com.az7car.watchcat.core.netty;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.CheckExecutor;
import com.az7car.watchcat.core.pipeline.CheckRegistry;
import com.az7car.watchcat.core.pipeline.EventPipeline;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PacketProcessor {

    private final ExecutorService executor;
    private final EventPipeline pipeline;
    private final WatchcatConfig config;
    private final CheckExecutor checkExecutor;

    public PacketProcessor(WatchcatConfig config) {
        this.config = config;
        this.executor = Executors.newScheduledThreadPool(
                config.getAsyncThreads(),
                r -> {
                    Thread t = new Thread(r, "Watchcat-Processor");
                    t.setDaemon(true);
                    t.setPriority(Thread.NORM_PRIORITY);
                    return t;
                }
        );
        this.pipeline = new EventPipeline();
        this.checkExecutor = new CheckExecutor();
    }

    public void enqueueMovement(Player player, ServerPlayer nmsPlayer, Packet<?> packet, CheckRegistry registry) {
        executor.submit(() -> {
            PlayerData data = PlayerData.getOrCreate(player);
            data.updatePosition(nmsPlayer);
            for (var check : registry.getMovementChecks()) {
                CheckResult result = checkExecutor.execute(check, player, data, packet, nmsPlayer);
                pipeline.emit(result, data, check);
            }
        });
    }

    public void enqueueCombat(Player player, ServerPlayer nmsPlayer, Packet<?> packet, CheckRegistry registry) {
        executor.submit(() -> {
            PlayerData data = PlayerData.getOrCreate(player);
            for (var check : registry.getCombatChecks()) {
                CheckResult result = checkExecutor.execute(check, player, data, packet, nmsPlayer);
                pipeline.emit(result, data, check);
            }
        });
    }

    public void enqueueWorld(Player player, ServerPlayer nmsPlayer, Packet<?> packet, CheckRegistry registry) {
        executor.submit(() -> {
            PlayerData data = PlayerData.getOrCreate(player);
            for (var check : registry.getWorldChecks()) {
                CheckResult result = checkExecutor.execute(check, player, data, packet, nmsPlayer);
                pipeline.emit(result, data, check);
            }
        });
    }

    public void enqueueMod(Player player, ServerPlayer nmsPlayer, Packet<?> packet, CheckRegistry registry) {
        executor.submit(() -> {
            PlayerData data = PlayerData.getOrCreate(player);
            for (var check : registry.getModChecks()) {
                CheckResult result = checkExecutor.execute(check, player, data, packet, nmsPlayer);
                pipeline.emit(result, data, check);
            }
        });
    }

    public void enqueuePacket(Player player, ServerPlayer nmsPlayer, Packet<?> packet, CheckRegistry registry) {
        executor.submit(() -> {
            PlayerData data = PlayerData.getOrCreate(player);
            data.recordPacket(System.nanoTime());
        });
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}