package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RaidCheck extends AbstractCheck {

    private final int maxPlayersPerIp;
    private final int maxSimilarNames;
    private final int raidWindowSeconds;
    private final boolean autoBanRaid;

    private final Map<String, List<String>> ipPlayerMap;
    private final Map<String, RaidEntry> raidDetections;

    private static class RaidEntry {
        long detectedAt;
        Set<String> players;

        RaidEntry() {
            this.detectedAt = System.currentTimeMillis();
            this.players = new HashSet<>();
        }
    }

    public RaidCheck(WatchcatConfig config) {
        super("Raid", "mod",
            config.getCheckWeight("mod.raid", 0.8),
            config.isCheckEnabled("mod.raid", true));
        this.maxPlayersPerIp = config.getInt("raid.max-players-per-ip", 3);
        this.maxSimilarNames = config.getInt("raid.max-similar-names", 4);
        this.raidWindowSeconds = config.getInt("raid.window-seconds", 60);
        this.autoBanRaid = config.getBoolean("raid.auto-ban", true);
        this.ipPlayerMap = new ConcurrentHashMap<>();
        this.raidDetections = new ConcurrentHashMap<>();
    }

    public boolean checkJoin(Player player, String ip) {
        if (ip == null) return false;
        String name = player.getName().toLowerCase();
        long now = System.currentTimeMillis();

        ipPlayerMap.computeIfAbsent(ip, k -> Collections.synchronizedList(new ArrayList<>())).add(name);
        List<String> names = ipPlayerMap.get(ip);

        names.removeIf(n -> false);
        int recent = 0;
        for (String n : names) {
            if (n != null) recent++;
        }

        if (recent > maxPlayersPerIp) {
            String raidKey = "ip:" + ip;
            RaidEntry entry = raidDetections.computeIfAbsent(raidKey, k -> new RaidEntry());
            entry.players.add(name);
            entry.detectedAt = now;

            if (autoBanRaid) {
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("Watchcat"), () -> {
                    for (String pn : entry.players) {
                        Player p = Bukkit.getPlayerExact(pn);
                        if (p != null && p.isOnline()) {
                            String reason = "Raid participation detected";
                            Bukkit.getBanList(org.bukkit.BanList.Type.NAME)
                                .addBan(p.getName(), reason, null, "Watchcat-Raid");
                            p.kickPlayer(reason);
                        }
                    }
                });
                return true;
            }
            return true;
        }

        if (name.length() >= 3) {
            String base = name.replaceAll("\\d+$", "");
            int similar = 0;
            for (Map.Entry<String, List<String>> entry : ipPlayerMap.entrySet()) {
                for (String n : entry.getValue()) {
                    String nb = n.replaceAll("\\d+$", "");
                    if (nb.equals(base) && !n.equals(name)) {
                        similar++;
                    }
                }
            }
            if (similar >= maxSimilarNames) {
                String raidKey = "similar:" + base;
                RaidEntry entry = raidDetections.computeIfAbsent(raidKey, k -> new RaidEntry());
                entry.players.add(name);
                entry.detectedAt = now;

                if (autoBanRaid) {
                    Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("Watchcat"), () -> {
                        for (String pn : entry.players) {
                            Player p = Bukkit.getPlayerExact(pn);
                            if (p != null && p.isOnline()) {
                                String reason = "Raid participation detected (similar names)";
                                Bukkit.getBanList(org.bukkit.BanList.Type.NAME)
                                    .addBan(p.getName(), reason, null, "Watchcat-Raid");
                                p.kickPlayer(reason);
                            }
                        }
                    });
                    return true;
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        return CheckResult.PASS;
    }
}
