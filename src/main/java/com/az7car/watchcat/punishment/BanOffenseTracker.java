package com.az7car.watchcat.punishment;

import com.az7car.watchcat.WatchcatPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class BanOffenseTracker {

    private final File storageFile;
    private final YamlConfiguration storage;
    private final Map<UUID, Integer> offenses;
    private final Map<String, Integer> ipOffenses;

    public BanOffenseTracker(WatchcatPlugin plugin) {
        this.storageFile = new File(plugin.getDataFolder(), "offenses.yml");
        this.storage = YamlConfiguration.loadConfiguration(storageFile);
        this.offenses = new HashMap<>();
        this.ipOffenses = new HashMap<>();
        load();
    }

    public int getOffenseCount(UUID uuid) {
        return offenses.getOrDefault(uuid, 0);
    }

    public int getIpOffenseCount(String ip) {
        return ipOffenses.getOrDefault(ip, 0);
    }

    public void incrementOffenses(UUID uuid, String ip) {
        offenses.merge(uuid, 1, Integer::sum);
        if (ip != null && !ip.isEmpty()) {
            ipOffenses.merge(ip, 1, Integer::sum);
        }
        save();
    }

    public double getDurationMultiplier(UUID uuid, String ip) {
        int count = Math.max(getOffenseCount(uuid), ip != null ? getIpOffenseCount(ip) : 0);
        return switch (count) {
            case 0, 1 -> 1.0;
            case 2 -> 2.0;
            case 3 -> 4.0;
            default -> -1.0;
        };
    }

    private void load() {
        if (storage.contains("offenses")) {
            for (String key : storage.getConfigurationSection("offenses").getKeys(false)) {
                offenses.put(UUID.fromString(key), storage.getInt("offenses." + key));
            }
        }
        if (storage.contains("ip_offenses")) {
            for (String key : storage.getConfigurationSection("ip_offenses").getKeys(false)) {
                ipOffenses.put(key, storage.getInt("ip_offenses." + key));
            }
        }
    }

    private void save() {
        for (Map.Entry<UUID, Integer> entry : offenses.entrySet()) {
            storage.set("offenses." + entry.getKey().toString(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : ipOffenses.entrySet()) {
            storage.set("ip_offenses." + entry.getKey(), entry.getValue());
        }
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            // silent
        }
    }
}
