package com.az7car.watchcat.core.config;

import com.az7car.watchcat.WatchcatPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.logging.Logger;

public class ConfigValidator {

    private final WatchcatPlugin plugin;
    private final Logger logger;
    private int errors;
    private int warnings;

    public ConfigValidator(WatchcatPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public boolean validate() {
        errors = 0;
        warnings = 0;
        FileConfiguration cfg = plugin.getConfig();

        validateSection(cfg, "discord", "discord.invite-link");
        validateSection(cfg, "ban-wave", "ban-wave.confirm-threshold");
        validateSection(cfg, "chat", "chat.max-messages-per-second");
        validateSection(cfg, "antibot", "antibot.max-joins-per-minute");
        validateSection(cfg, "raid", "raid.max-players-per-ip");
        validateSection(cfg, "punishment.severity", "punishment.severity.critical");
        validateSection(cfg, "performance", "performance.async-threads");
        validateSection(cfg, "ml", "ml.enabled");

        validateCheckSection("combat", cfg);
        validateCheckSection("movement", cfg);
        validateCheckSection("world", cfg);
        validateCheckSection("mod", cfg);

        int asyncThreads = cfg.getInt("performance.async-threads", 4);
        if (asyncThreads < 1 || asyncThreads > 16) {
            warn("performance.async-threads should be 1-16, got " + asyncThreads);
        }

        double threshold = cfg.getDouble("ban-wave.confirm-threshold", 0.8);
        if (threshold < 0.1 || threshold > 1.0) {
            warn("ban-wave.confirm-threshold should be 0.1-1.0, got " + threshold);
        }

        if (errors > 0) {
            logger.severe("Config validation failed with " + errors + " error(s) and " + warnings + " warning(s)");
            return false;
        }
        if (warnings > 0) {
            logger.warning("Config validated with " + warnings + " warning(s)");
        } else {
            logger.info("Config validation passed.");
        }
        return true;
    }

    private void validateSection(FileConfiguration cfg, String section, String key) {
        if (!cfg.isConfigurationSection(section) && section.equals(key)) {
            if (!cfg.contains(key)) {
                error("Missing required config key: " + key);
            }
        } else if (section.equals(key)) {
            if (!cfg.contains(key)) {
                error("Missing required config key: " + key);
            }
        }
    }

    private void validateCheckSection(String category, FileConfiguration cfg) {
        ConfigurationSection section = cfg.getConfigurationSection("checks." + category);
        if (section == null) {
            warn("Missing checks." + category + " section");
            return;
        }
        for (String key : section.getKeys(false)) {
            String path = "checks." + category + "." + key;
            if (!cfg.contains(path + ".enabled")) {
                warn("Check " + path + " missing enabled flag");
            }
            if (!cfg.contains(path + ".weight")) {
                warn("Check " + path + " missing weight");
            }
        }
    }

    private void error(String msg) { errors++; logger.severe("[Config] " + msg); }
    private void warn(String msg) { warnings++; logger.warning("[Config] " + msg); }
}
