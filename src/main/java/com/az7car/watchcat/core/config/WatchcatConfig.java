package com.az7car.watchcat.core.config;

import com.az7car.watchcat.WatchcatPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.logging.Logger;

public class WatchcatConfig {

    private final FileConfiguration config;
    private final Logger logger;

    public WatchcatConfig(WatchcatPlugin plugin) {
        this.config = plugin.getConfig();
        this.logger = plugin.getLogger();
    }

    public void reload() {
        WatchcatPlugin.getInstance().reloadConfig();
    }

    public String getDiscordLink() {
        return config.getString("discord.invite-link", "https://orvexsmp.net/discord");
    }

    public String getDiscordServerName() {
        return config.getString("discord.server-name", "OrvexSMP");
    }

    // Ban-wave
    public boolean isBanWaveEnabled() { return config.getBoolean("ban-wave.enabled", false); }
    public boolean isInstantBan() { return config.getBoolean("ban-wave.instant-ban", true); }
    public long getBanWaveMinDelay() { return config.getLong("ban-wave.min-delay-minutes", 30); }
    public long getBanWaveMaxDelay() { return config.getLong("ban-wave.max-delay-minutes", 360); }
    public long getBanWaveJitter() { return config.getLong("ban-wave.jitter-seconds", 300); }
    public double getConfirmThreshold() { return config.getDouble("ban-wave.confirm-threshold", 0.8); }

    // Appeal
    public boolean isAppealEnabled() { return config.getBoolean("appeal.enabled", true); }
    public int getAppealCodeLength() { return config.getInt("appeal.code-length", 7); }
    public String getAppealCharacters() {
        return config.getString("appeal.characters", "ABCDEFGHJKLMNPQRSTUVWXYZ23456789");
    }
    public String getAppealStorageFile() { return config.getString("appeal.storage-file", "appeals.yml"); }

    // Performance
    public int getAsyncThreads() { return config.getInt("performance.async-threads", 4); }
    public boolean isMlEnabled() { return config.getBoolean("performance.enable-ml", true); }
    public boolean isDebugMode() { return config.getBoolean("performance.debug-mode", false); }

    // ML
    public String getModelPath() { return config.getString("ml.model-path", "model/isolation_forest.onnx"); }
    public int getMLInferenceInterval() { return config.getInt("ml.inference-interval-ticks", 20); }
    public double getHeuristicWeight() { return config.getDouble("ml.heuristic-weight", 0.5); }
    public double getMLWeight() { return config.getDouble("ml.ml-weight", 0.5); }

    // Check config access
    public ConfigurationSection getSection(String path) {
        return config.getConfigurationSection(path);
    }

    public double getCheckDouble(String checkPath, String key, double def) {
        return config.getDouble("checks." + checkPath + "." + key, def);
    }

    public boolean isCheckEnabled(String checkPath) {
        return config.getBoolean("checks." + checkPath + ".enabled", true);
    }

    public boolean isCheckEnabled(String checkPath, boolean def) {
        return config.getBoolean("checks." + checkPath + ".enabled", def);
    }

    public double getCheckWeight(String checkPath) {
        return config.getDouble("checks." + checkPath + ".weight", 1.0);
    }

    public double getCheckWeight(String checkPath, double def) {
        return config.getDouble("checks." + checkPath + ".weight", def);
    }

    public String getCheckSeverity(String checkPath) {
        return config.getString("checks." + checkPath + ".severity", "medium");
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }
}
