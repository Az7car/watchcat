package com.az7car.watchcat;

import com.az7car.watchcat.core.command.WatchcatCommand;
import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.di.WatchcatInjector;
import com.az7car.watchcat.core.netty.PacketInjector;
import com.az7car.watchcat.core.pipeline.CheckRegistry;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.ml.AnomalyDetector;
import com.az7car.watchcat.ml.ONNXInference;
import com.az7car.watchcat.punishment.AppealCodeManager;
import com.az7car.watchcat.punishment.BanWaveExecutor;
import com.az7car.watchcat.punishment.ChatPunishmentManager;
import com.az7car.watchcat.punishment.ShadowFlaggingSystem;
import com.az7car.watchcat.util.TPSMonitor;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;

public class WatchcatPlugin extends JavaPlugin {

    private static WatchcatPlugin instance;

    private WatchcatInjector injector;
    private WatchcatConfig config;
    private PacketInjector packetInjector;
    private CheckRegistry checkRegistry;
    private ShadowFlaggingSystem flaggingSystem;
    private BanWaveExecutor banWaveExecutor;
    private AppealCodeManager appealCodeManager;
    private ONNXInference onnxInference;
    private AnomalyDetector anomalyDetector;
    private TPSMonitor tpsMonitor;
    private ChatPunishmentManager chatPunishmentManager;

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        saveResource("model/isolation_forest.onnx", false);
    }

    @Override
    public void onEnable() {
        this.injector = new WatchcatInjector();
        this.config = injector.createConfig();
        this.tpsMonitor = injector.createTPSMonitor();

        this.onnxInference = config.isMlEnabled() ? injector.createONNXInference(config) : null;
        this.anomalyDetector = config.isMlEnabled() ? injector.createAnomalyDetector(onnxInference) : null;
        this.appealCodeManager = injector.createAppealCodeManager(config, this);
        this.flaggingSystem = injector.createFlaggingSystem();
        this.chatPunishmentManager = injector.createChatPunishmentManager(config, this);
        this.banWaveExecutor = injector.createBanWaveExecutor(config, this);
        this.checkRegistry = injector.createCheckRegistry(config);
        this.packetInjector = injector.createPacketInjector(this, config, checkRegistry);

        injector.registerChecks(checkRegistry, config, anomalyDetector, flaggingSystem, chatPunishmentManager);

        getServer().getPluginManager().registerEvents(new com.az7car.watchcat.core.netty.PlayerListener(packetInjector, flaggingSystem, banWaveExecutor, appealCodeManager, chatPunishmentManager, injector.createAntiBotCheck(config), injector.createRaidCheck(config)), this);

        packetInjector.injectAll();

        var watchcatCmd = new WatchcatCommand(this, checkRegistry, flaggingSystem, banWaveExecutor);
        getCommand("watchcat").setExecutor(watchcatCmd);
        getCommand("watchcat").setTabCompleter(watchcatCmd);

        tpsMonitor.start(this);

        getLogger().info("Watchcat by @Az7car enabled. Protecting " + getServer().getOnlinePlayers().size() + " players.");
    }

    @Override
    public void onDisable() {
        if (packetInjector != null) packetInjector.shutdown();
        if (tpsMonitor != null) tpsMonitor.stop();
        if (onnxInference != null) onnxInference.close();
        PlayerData.cleanup();
        instance = null;
        getLogger().info("Watchcat disabled.");
    }

    public static WatchcatPlugin getInstance() { return instance; }
    public WatchcatConfig getWatchcatConfig() { return config; }
    public PacketInjector getPacketInjector() { return packetInjector; }
    public CheckRegistry getCheckRegistry() { return checkRegistry; }
    public ShadowFlaggingSystem getFlaggingSystem() { return flaggingSystem; }
    public BanWaveExecutor getBanWaveExecutor() { return banWaveExecutor; }
    public AppealCodeManager getAppealCodeManager() { return appealCodeManager; }
    public TPSMonitor getTpsMonitor() { return tpsMonitor; }
    public ChatPunishmentManager getChatPunishmentManager() { return chatPunishmentManager; }
}
