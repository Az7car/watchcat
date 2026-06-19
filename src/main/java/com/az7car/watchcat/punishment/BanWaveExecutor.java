package com.az7car.watchcat.punishment;

import com.az7car.watchcat.WatchcatPlugin;
import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.core.pipeline.EventPipeline;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class BanWaveExecutor {

    private final WatchcatConfig config;
    private final WatchcatPlugin plugin;
    private final SecureRandom secureRandom;
    private final BanOffenseTracker offenseTracker;
    private final Logger logger;
    private final Map<UUID, BanEntry> pendingBans;
    private boolean waveInProgress;

    private static final Map<String, Integer> SEVERITY_DAYS = Map.of(
        "critical", 365,
        "high", 180,
        "medium", 90,
        "low", 30
    );

    private record BanEntry(UUID uuid, String name, String ip, String cheat, String severity, String appealCode) {}

    public BanWaveExecutor(WatchcatConfig config, WatchcatPlugin plugin) {
        this.config = config;
        this.plugin = plugin;
        this.secureRandom = new SecureRandom();
        this.offenseTracker = new BanOffenseTracker(plugin);
        this.pendingBans = new ConcurrentHashMap<>();
        this.logger = plugin.getLogger();
        this.waveInProgress = false;

        EventPipeline pipeline = new EventPipeline();
        pipeline.subscribe(this::onCheckResult);
        if (config.isBanWaveEnabled()) {
            scheduleNextWave();
        }
    }

    private void onCheckResult(CheckResult result, PlayerData data, AbstractCheck check) {
        if (result != CheckResult.FLAG) return;
        UUID uuid = data.getUuid();
        double score = plugin.getFlaggingSystem().getScore(uuid);

        if (score >= config.getConfirmThreshold()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;

            String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : null;
            String cheat = plugin.getFlaggingSystem().getPrimaryCheat(uuid);
            String severity = plugin.getFlaggingSystem().getPrimarySeverity(uuid);
            String appealCode = plugin.getAppealCodeManager().getOrCreateCode(uuid, ip);

            BanEntry entry = new BanEntry(uuid, player.getName(), ip, cheat, severity, appealCode);
            offenseTracker.incrementOffenses(uuid, ip);

            if (config.isInstantBan()) {
                banPlayer(entry);
            } else {
                pendingBans.put(uuid, entry);
            }
        }
    }

    private void scheduleNextWave() {
        if (!config.isBanWaveEnabled()) return;

        long minDelay = config.getBanWaveMinDelay() * 60 * 1000;
        long maxDelay = config.getBanWaveMaxDelay() * 60 * 1000;
        long delay = minDelay + (long)(secureRandom.nextDouble() * (maxDelay - minDelay));

        new BukkitRunnable() {
            @Override
            public void run() {
                executeWave();
                scheduleNextWave();
            }
        }.runTaskLater(plugin, delay / 50);
    }

    public void executeWave() {
        if (pendingBans.isEmpty() || waveInProgress) return;
        waveInProgress = true;

        List<BanEntry> wave = new ArrayList<>(pendingBans.values());
        Collections.shuffle(wave, secureRandom);

        long jitterMs = config.getBanWaveJitter() * 1000;
        long baseDelay = 0;

        for (BanEntry entry : wave) {
            long jitter = (long)(secureRandom.nextDouble() * jitterMs);
            long finalBaseDelay = baseDelay;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                banPlayer(entry);
                pendingBans.remove(entry.uuid);
            }, (finalBaseDelay + jitter) / 50);

            baseDelay += 200;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            waveInProgress = false;
        }, (baseDelay + jitterMs + 1000) / 50);
    }

    private void banPlayer(BanEntry entry) {
        Player player = Bukkit.getPlayer(entry.uuid);
        String discordLink = config.getDiscordLink();
        String serverName = config.getDiscordServerName();

        int baseDays = SEVERITY_DAYS.getOrDefault(entry.severity, 30);
        double multiplier = offenseTracker.getDurationMultiplier(entry.uuid, entry.ip);
        long banDuration;

        String reason;
        if (multiplier < 0) {
            banDuration = -1;
            reason = "You have been permanently banned from " + serverName + ".\n"
                + "Cheat detected: " + entry.cheat + " (" + entry.severity.toUpperCase() + ")\n"
                + "Offense count: " + offenseTracker.getOffenseCount(entry.uuid) + "\n"
                + "Appeal Code: " + entry.appealCode + "\n"
                + "Appeal at: " + discordLink;
        } else {
            banDuration = (long)(baseDays * multiplier);
            String durationStr = banDuration + " days";
            reason = "You have been banned from " + serverName + " for " + durationStr + ".\n"
                + "Cheat detected: " + entry.cheat + " (" + entry.severity.toUpperCase() + ")\n"
                + "Offense count: " + offenseTracker.getOffenseCount(entry.uuid) + "\n"
                + "Appeal Code: " + entry.appealCode + "\n"
                + "Appeal at: " + discordLink;
        }

        Bukkit.getBanList(org.bukkit.BanList.Type.NAME)
            .addBan(entry.name, reason, banDuration > 0 ? new Date(System.currentTimeMillis() + banDuration * 24 * 60 * 60 * 1000L) : null, "Watchcat");

        if (config.getBoolean("punishment.ip-ban", true) && entry.ip != null && !entry.ip.equals("127.0.0.1")) {
            Bukkit.getBanList(org.bukkit.BanList.Type.IP)
                .addBan(entry.ip, reason, banDuration > 0 ? new Date(System.currentTimeMillis() + banDuration * 24 * 60 * 60 * 1000L) : null, "Watchcat");
        }

        if (player != null && player.isOnline()) {
            player.kick(Component.text(reason).color(NamedTextColor.RED));
        }

        logger.info("Watchcat banned " + entry.name + " [" + entry.cheat + "/" + entry.severity
            + "] code=" + entry.appealCode + " duration=" + (banDuration > 0 ? banDuration + "d" : "permanent")
            + " offense#" + offenseTracker.getOffenseCount(entry.uuid));
    }

    public void checkPendingBan(Player player) {
        BanEntry entry = pendingBans.get(player.getUniqueId());
        if (entry != null) {
            banPlayer(entry);
        }
    }

    public BanOffenseTracker getOffenseTracker() { return offenseTracker; }
}
