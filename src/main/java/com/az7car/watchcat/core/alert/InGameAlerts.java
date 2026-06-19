package com.az7car.watchcat.core.alert;

import com.az7car.watchcat.WatchcatPlugin;
import com.az7car.watchcat.core.command.WatchcatCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class InGameAlerts {

    private static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.RED + "Watchcat" + ChatColor.GRAY + "] ";

    public static void flag(String playerName, String checkName, String category, double weight) {
        String msg = PREFIX + ChatColor.YELLOW + playerName + ChatColor.WHITE
            + " flagged " + ChatColor.RED + checkName
            + ChatColor.GRAY + " (" + category + ", w=" + String.format("%.1f", weight) + ")";

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("watchcat.alerts")) {
                WatchcatCommand cmd = getCommand();
                if (cmd == null || cmd.hasAlertsOn(p)) {
                    p.sendMessage(msg);
                }
            }
        }
        Bukkit.getLogger().info("[Watchcat] " + playerName + " flagged " + checkName);
    }

    public static void ban(String playerName, String reason, int durationDays) {
        String msg = PREFIX + ChatColor.RED + playerName + ChatColor.WHITE
            + " banned. Reason: " + reason
            + ChatColor.GRAY + " (" + durationDays + "d)";

        Bukkit.broadcast(msg, "watchcat.alerts");
        Bukkit.getLogger().info("[Watchcat] " + playerName + " banned: " + reason + " (" + durationDays + "d)");
    }

    public static void punish(String playerName, String action, String reason) {
        String msg = PREFIX + ChatColor.YELLOW + playerName + ChatColor.WHITE
            + " " + action + ". Reason: " + reason;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("watchcat.alerts")) {
                p.sendMessage(msg);
            }
        }
        Bukkit.getLogger().info("[Watchcat] " + playerName + " " + action + ": " + reason);
    }

    private static WatchcatCommand getCommand() {
        try {
            var cmd = WatchcatPlugin.getInstance().getCommand("watchcat");
            if (cmd != null && cmd.getExecutor() instanceof WatchcatCommand wc) {
                return wc;
            }
        } catch (Exception e) {}
        return null;
    }
}
