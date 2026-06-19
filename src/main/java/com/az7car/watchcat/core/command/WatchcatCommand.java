package com.az7car.watchcat.core.command;

import com.az7car.watchcat.WatchcatPlugin;
import com.az7car.watchcat.core.falsepositive.FPDetector;
import com.az7car.watchcat.core.falsepositive.FPStatistics;
import com.az7car.watchcat.core.lag.LatencyTracker;
import com.az7car.watchcat.core.netty.TickTimerAnalyzer;
import com.az7car.watchcat.core.pipeline.CheckProfiler;
import com.az7car.watchcat.core.pipeline.CheckRegistry;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.punishment.BanWaveExecutor;
import com.az7car.watchcat.punishment.ShadowFlaggingSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class WatchcatCommand implements CommandExecutor, TabCompleter {

    private final WatchcatPlugin plugin;
    private final CheckRegistry registry;
    private final ShadowFlaggingSystem flaggingSystem;
    private final BanWaveExecutor banWaveExecutor;
    private final Set<UUID> alertToggles;
    private final long startTime;

    public WatchcatCommand(WatchcatPlugin plugin, CheckRegistry registry,
                           ShadowFlaggingSystem flaggingSystem, BanWaveExecutor banWaveExecutor) {
        this.plugin = plugin;
        this.registry = registry;
        this.flaggingSystem = flaggingSystem;
        this.banWaveExecutor = banWaveExecutor;
        this.alertToggles = new HashSet<>();
        this.startTime = System.currentTimeMillis();
    }

    public boolean hasAlertsOn(Player player) { return alertToggles.contains(player.getUniqueId()); }
    public void toggleAlerts(Player player) {
        if (!alertToggles.remove(player.getUniqueId())) alertToggles.add(player.getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /watchcat <alerts|stats|profile|checks|player|reload|whitelist|report|banwave|reason>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "alerts":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use /watchcat alerts");
                    return true;
                }
                if (!player.hasPermission("watchcat.alerts")) {
                    player.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                toggleAlerts(player);
                player.sendMessage(ChatColor.GREEN + "Alerts " + (hasAlertsOn(player) ? "enabled" : "disabled"));
                return true;

            case "stats":
                if (!sender.hasPermission("watchcat.stats")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "=== Watchcat Stats ===");
                sender.sendMessage(ChatColor.YELLOW + "Total checks: " + registry.getChecks().size());
                int tracked = flaggingSystem.getTrackedPlayerCount();
                sender.sendMessage(ChatColor.YELLOW + "Flagged players: " + tracked);
                var allStats = FPStatistics.getAllStats();
                sender.sendMessage(ChatColor.YELLOW + "Checks with FP data: " + allStats.size());
                for (var e : allStats.entrySet()) {
                    var s = e.getValue();
                    sender.sendMessage(ChatColor.GRAY + "  " + e.getKey() + ": flags=" + s.totalFlags.get()
                        + " fp=" + s.falsePositives.get() + " confirmed=" + s.confirmedFlags.get()
                        + " accuracy=" + String.format("%.0f%%", s.getAccuracy() * 100));
                }
                return true;

            case "player":
                if (!sender.hasPermission("watchcat.stats")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /watchcat player <name>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                UUID pid = target.getUniqueId();
                PlayerData pd = PlayerData.get(pid);
                sender.sendMessage(ChatColor.GOLD + "=== Player: " + target.getName() + " ===");
                if (pd != null) {
                    sender.sendMessage(ChatColor.YELLOW + "Health: " + pd.getHealth());
                    sender.sendMessage(ChatColor.YELLOW + "Air ticks: " + pd.getAirTicks());
                    sender.sendMessage(ChatColor.YELLOW + "Ground: " + pd.isOnGround());
                    sender.sendMessage(ChatColor.YELLOW + "Sprinting: " + pd.isSprinting());
                    sender.sendMessage(ChatColor.YELLOW + "Sneaking: " + pd.isSneaking());
                    sender.sendMessage(ChatColor.YELLOW + "Cancelled packets: " + pd.cancelledPackets);
                    sender.sendMessage(ChatColor.YELLOW + "Swing count: " + pd.getSwingCount());
                }
                double score = flaggingSystem.getScore(pid);
                sender.sendMessage(ChatColor.YELLOW + "Flag score: " + String.format("%.2f", score));
                long ping = LatencyTracker.getSmoothedPing(pid);
                sender.sendMessage(ChatColor.YELLOW + "Smoothed ping: " + ping + "ms");
                double jitter = LatencyTracker.getPingJitter(pid);
                sender.sendMessage(ChatColor.YELLOW + "Ping jitter: " + String.format("%.1f", jitter) + "ms");
                double timerMean = TickTimerAnalyzer.getIntervalMean(pid);
                double timerStd = TickTimerAnalyzer.getIntervalStd(pid);
                sender.sendMessage(ChatColor.YELLOW + "Timer mean: " + String.format("%.1f", timerMean) + "ms std: " + String.format("%.1f", timerStd) + "ms");
                return true;

            case "profile":
                if (!sender.hasPermission("watchcat.profile")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "=== Check Profiler ===");
                var slowest = CheckProfiler.getSlowest(10);
                for (var e : slowest) {
                    var t = e.getValue();
                    sender.sendMessage(ChatColor.GRAY + "  " + e.getKey()
                        + " avg=" + String.format("%.1f", t.getAverageMicros()) + "us"
                        + " max=" + String.format("%.1f", t.getMaxMicros()) + "us"
                        + " calls=" + t.count);
                }
                return true;

            case "checks":
                if (!sender.hasPermission("watchcat.stats")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "=== Registered Checks ===");
                sender.sendMessage(ChatColor.YELLOW + "Combat: " + registry.getCombatChecks().size());
                sender.sendMessage(ChatColor.YELLOW + "Movement: " + registry.getMovementChecks().size());
                sender.sendMessage(ChatColor.YELLOW + "World: " + registry.getWorldChecks().size());
                sender.sendMessage(ChatColor.YELLOW + "Mod: " + registry.getModChecks().size());
                sender.sendMessage(ChatColor.YELLOW + "Total: " + registry.getChecks().size());
                if (args.length >= 2) {
                    String cat = args[1].toLowerCase();
                    var list = switch (cat) {
                        case "combat" -> registry.getCombatChecks();
                        case "movement" -> registry.getMovementChecks();
                        case "world" -> registry.getWorldChecks();
                        case "mod" -> registry.getModChecks();
                        default -> null;
                    };
                    if (list != null) {
                        for (var c : list) {
                            sender.sendMessage(ChatColor.GRAY + "  " + (c.isEnabled() ? ChatColor.GREEN : ChatColor.RED) + c.getName());
                        }
                    }
                }
                return true;

            case "reload":
                if (!sender.hasPermission("watchcat.reload")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
                return true;

            case "whitelist":
                if (!sender.hasPermission("watchcat.whitelist")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /watchcat whitelist <checkName> <playerName>");
                    return true;
                }
                var whitelistTarget = plugin.getServer().getPlayer(args[2]);
                if (whitelistTarget == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "Whitelisted " + whitelistTarget.getName() + " for " + args[1]);
                return true;

            case "report":
                if (!sender.hasPermission("watchcat.report")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /watchcat report <checkName> <playerName>");
                    return true;
                }
                var repTarget = plugin.getServer().getPlayer(args[2]);
                if (repTarget == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                FPStatistics.recordFP(args[1], repTarget.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "False positive report recorded for " + args[1] + " on " + repTarget.getName());
                return true;

            case "banwave":
                if (!sender.hasPermission("watchcat.banwave")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                banWaveExecutor.executeWave();
                sender.sendMessage(ChatColor.GREEN + "Ban wave executed.");
                return true;

            case "reason":
                if (!sender.hasPermission("watchcat.banwave")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /watchcat reason <player> <reason>");
                    return true;
                }
                String reasonTarget = args[1];
                String banReason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                banWaveExecutor.banPlayerWithReason(reasonTarget, banReason, "high");
                sender.sendMessage(ChatColor.GREEN + "Banned " + reasonTarget + " with reason: " + banReason);
                return true;

            case "info":
                if (!sender.hasPermission("watchcat.stats")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                long uptime = (System.currentTimeMillis() - startTime) / 1000;
                long hours = uptime / 3600;
                long mins = (uptime % 3600) / 60;
                sender.sendMessage(ChatColor.GOLD + "=== Watchcat Info ===");
                sender.sendMessage(ChatColor.YELLOW + "Version: 1.0.0");
                sender.sendMessage(ChatColor.YELLOW + "Uptime: " + hours + "h " + mins + "m");
                sender.sendMessage(ChatColor.YELLOW + "Total checks: " + registry.getChecks().size());
                sender.sendMessage(ChatColor.YELLOW + "Online players: " + Bukkit.getOnlinePlayers().size());
                sender.sendMessage(ChatColor.YELLOW + "Tracked players: " + flaggingSystem.getTrackedPlayerCount());
                sender.sendMessage(ChatColor.YELLOW + "Paper: 1.21.11 | Java 21");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use: alerts, stats, profile, checks, player, reload, whitelist, report, banwave, reason, info");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("alerts", "stats", "profile", "checks", "player", "reload", "whitelist", "report", "banwave", "reason", "info").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("checks")) {
            return Arrays.asList("combat", "movement", "world", "mod");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("whitelist") || args[0].equalsIgnoreCase("report"))) {
            return registry.getChecks().stream().map(c -> c.getClass().getSimpleName().replace("Check", "").toLowerCase())
                .filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("whitelist") || args[0].equalsIgnoreCase("report"))) {
            return null;
        }
        return List.of();
    }
}
