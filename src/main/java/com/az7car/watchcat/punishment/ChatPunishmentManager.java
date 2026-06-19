package com.az7car.watchcat.punishment;

import com.az7car.watchcat.WatchcatPlugin;
import com.az7car.watchcat.core.config.WatchcatConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatPunishmentManager {

    private final WatchcatConfig config;
    private final WatchcatPlugin plugin;
    private final Map<UUID, MuteEntry> mutes;
    private final Map<UUID, WarningEntry> warnings;

    private record MuteEntry(long until, String reason) {}
    private record WarningEntry(int count, long lastWarning) {}

    public ChatPunishmentManager(WatchcatConfig config, WatchcatPlugin plugin) {
        this.config = config;
        this.plugin = plugin;
        this.mutes = new ConcurrentHashMap<>();
        this.warnings = new ConcurrentHashMap<>();
    }

    public boolean isMuted(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        if (entry == null) return false;
        if (System.currentTimeMillis() >= entry.until) {
            mutes.remove(uuid);
            return false;
        }
        return true;
    }

    public String getMuteReason(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        return entry != null ? entry.reason : null;
    }

    public long getMuteRemaining(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        if (entry == null) return 0;
        long remaining = entry.until - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    public Result warnPlayer(Player player, String reason) {
        UUID uuid = player.getUniqueId();
        WarningEntry entry = warnings.getOrDefault(uuid, new WarningEntry(0, 0));
        long now = System.currentTimeMillis();

        if (now - entry.lastWarning > config.getInt("chat.warning-decay-seconds", 300) * 1000L) {
            entry = new WarningEntry(0, 0);
        }

        int newCount = entry.count + 1;
        warnings.put(uuid, new WarningEntry(newCount, now));
        int maxWarnings = config.getInt("chat.max-warnings-before-ban", 3);

        player.sendMessage(Component.text("[Watchcat] Warning: " + reason)
            .color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("[Watchcat] Warning " + newCount + "/" + maxWarnings)
            .color(NamedTextColor.GRAY));

        if (newCount >= maxWarnings) {
            int banDuration = config.getInt("chat.temp-ban-duration-seconds", 3600) * newCount;
            Date expires = new Date(System.currentTimeMillis() + banDuration * 1000L);
            String banReason = "Temporarily banned for repeated violations: " + reason;
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME)
                .addBan(player.getName(), banReason, expires, "Watchcat");
            player.kick(Component.text(banReason).color(NamedTextColor.RED));
            warnings.remove(uuid);
            mutes.remove(uuid);
            Bukkit.getLogger().info("Watchcat temp-banned " + player.getName() + " for " + banDuration + "s: " + reason);
            return Result.BANNED;
        }

        long muteDuration = config.getInt("chat.mute-duration-seconds", 120) * 1000L;
        mutes.put(uuid, new MuteEntry(now + muteDuration, reason));
        player.sendMessage(Component.text("[Watchcat] You have been muted for " + (muteDuration / 1000) + " seconds.")
            .color(NamedTextColor.RED));

        return Result.MUTED;
    }

    public boolean checkJoinBan(Player player) {
        UUID uuid = player.getUniqueId();
        WarningEntry entry = warnings.get(uuid);
        if (entry == null) return false;
        long now = System.currentTimeMillis();
        if (now - entry.lastWarning > config.getInt("chat.warning-decay-seconds", 300) * 1000L) {
            warnings.remove(uuid);
            return false;
        }
        return true;
    }

    public enum Result { WARNED, MUTED, BANNED, PASS }
}
