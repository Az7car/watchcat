package com.az7car.watchcat.punishment;

import com.az7car.watchcat.WatchcatPlugin;
import com.az7car.watchcat.core.config.WatchcatConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

public class AppealCodeManager {

    private final String charset;
    private final int codeLength;
    private final File storageFile;
    private final YamlConfiguration storage;
    private final SecureRandom random;
    private final boolean enabled;

    public AppealCodeManager(WatchcatConfig config, WatchcatPlugin plugin) {
        this.charset = config.getAppealCharacters();
        this.codeLength = config.getAppealCodeLength();
        this.enabled = config.isAppealEnabled();
        this.storageFile = new File(plugin.getDataFolder(), config.getAppealStorageFile());
        this.storage = YamlConfiguration.loadConfiguration(storageFile);
        this.random = new SecureRandom();
    }

    public String getOrCreateCode(UUID uuid, String ip) {
        if (!enabled) return "DISABLED";

        String existing = storage.getString("uuid_codes." + uuid.toString());
        if (existing != null) return existing;

        String code = generateUniqueCode();
        storage.set("uuid_codes." + uuid.toString(), code);
        storage.set("code_uuids." + code, uuid.toString());
        storage.set("code_ips." + code, ip != null ? ip : "unknown");
        save();
        return code;
    }

    public String getCode(UUID uuid) {
        return storage.getString("uuid_codes." + uuid.toString());
    }

    public UUID getUUIDFromCode(String code) {
        String uuidStr = storage.getString("code_uuids." + code.toUpperCase());
        if (uuidStr == null) return null;
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getIPFromCode(String code) {
        return storage.getString("code_ips." + code.toUpperCase());
    }

    private String generateUniqueCode() {
        Set<String> existing = new HashSet<>(storage.getConfigurationSection("code_uuids").getKeys(false));
        StringBuilder code;
        do {
            code = new StringBuilder(codeLength);
            for (int i = 0; i < codeLength; i++) {
                code.append(charset.charAt(random.nextInt(charset.length())));
            }
        } while (existing.contains(code.toString()));
        return code.toString();
    }

    private void save() {
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            // silent
        }
    }
}
