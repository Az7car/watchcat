package com.az7car.watchcat.core.command;

import com.az7car.watchcat.WatchcatPlugin;
import com.az7car.watchcat.core.falsepositive.FPStatistics;
import com.az7car.watchcat.core.pipeline.CheckProfiler;
import com.az7car.watchcat.core.pipeline.CheckRegistry;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.punishment.BanWaveExecutor;
import com.az7car.watchcat.punishment.ShadowFlaggingSystem;
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

    public WatchcatCommand(WatchcatPlugin plugin, CheckRegistry registry,
                           ShadowFlaggingSystem flaggingSystem, BanWaveExecutor banWaveExecutor) {
        this.plugin = plugin;
        this.registry = registry;
        this.flaggingSystem = flaggingSystem;
        this.banWaveExecutor = banWaveExecutor;
        this.alertToggles = new HashSet<>();
    }

    public boolean hasAlertsOn(Player player) { return alertToggles.contains(player.getUniqueId()); }
    public void toggleAlerts(Player player) {
        if (!alertToggles.remove(player.getUniqueId())) alertToggles.add(player.getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /watchcat <alerts|stats|reload|whitelist|report|banwave>");
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
                sender.sendMessage(ChatColor.YELLOW + "Flagged players: " + flaggingSystem.getTrackedPlayerCount());
                var allStats = FPStatistics.getAllStats();
                sender.sendMessage(ChatColor.YELLOW + "Checks with FP data: " + allStats.size());
                for (var e : allStats.entrySet()) {
                    var s = e.getValue();
                    sender.sendMessage(ChatColor.GRAY + "  " + e.getKey() + ": flags=" + s.totalFlags.get()
                        + " fp=" + s.falsePositives.get() + " confirmed=" + s.confirmedFlags.get()
                        + " accuracy=" + String.format("%.0f%%", s.getAccuracy() * 100));
                }
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
                var target = plugin.getServer().getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "Whitelisted " + target.getName() + " for " + args[1]);
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
                int count = banWaveExecutor.executeWave();
                sender.sendMessage(ChatColor.GREEN + "Ban wave executed. " + count + " players banned.");
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use: alerts, stats, profile, reload, whitelist, report, banwave");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("alerts", "stats", "profile", "reload", "whitelist", "report", "banwave").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
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
