package com.az7car.watchcat.core.alert;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AlertManager {

    private static AlertManager instance;
    private final Set<UUID> alertToggled = ConcurrentHashMap.newKeySet();
    private boolean broadcastAlerts = true;

    public AlertManager() {
        instance = this;
    }

    public static AlertManager getInstance() {
        return instance;
    }

    public void alert(String checkName, String playerName, String details, String severity, double confidence) {
        String prefix = ChatColor.GRAY + "[" + ChatColor.RED + "Watchcat" + ChatColor.GRAY + "] ";
        String sevColor = switch (severity.toLowerCase()) {
            case "critical" -> ChatColor.DARK_RED.toString();
            case "high" -> ChatColor.RED.toString();
            case "medium" -> ChatColor.GOLD.toString();
            default -> ChatColor.YELLOW.toString();
        };
        String message = prefix + sevColor + playerName + ChatColor.GRAY +
            " failed " + ChatColor.WHITE + checkName + ChatColor.GRAY +
            " (" + details + ") " + ChatColor.YELLOW + String.format("%.1f", confidence * 100) + "%";

        if (broadcastAlerts) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.hasPermission("watchcat.alerts") && alertToggled.contains(online.getUniqueId())) {
                    online.sendMessage(message);
                }
            }
        }
    }

    public void toggleAlerts(Player player) {
        UUID uuid = player.getUniqueId();
        if (alertToggled.contains(uuid)) {
            alertToggled.remove(uuid);
            player.sendMessage(ChatColor.RED + "Watchcat alerts disabled.");
        } else {
            alertToggled.add(uuid);
            player.sendMessage(ChatColor.GREEN + "Watchcat alerts enabled.");
        }
    }

    public boolean hasAlertsToggled(Player player) {
        return alertToggled.contains(player.getUniqueId());
    }
}
