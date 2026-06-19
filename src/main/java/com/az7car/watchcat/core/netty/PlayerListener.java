package com.az7car.watchcat.core.netty;

import com.az7car.watchcat.WatchcatPlugin;
import com.az7car.watchcat.detection.mod.AntiBotCheck;
import com.az7car.watchcat.detection.mod.RaidCheck;
import com.az7car.watchcat.punishment.AppealCodeManager;
import com.az7car.watchcat.punishment.BanWaveExecutor;
import com.az7car.watchcat.punishment.ChatPunishmentManager;
import com.az7car.watchcat.punishment.ShadowFlaggingSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.InetSocketAddress;

public class PlayerListener implements Listener {

    private final PacketInjector injector;
    private final ShadowFlaggingSystem flaggingSystem;
    private final BanWaveExecutor banWaveExecutor;
    private final AppealCodeManager appealCodeManager;
    private final ChatPunishmentManager chatPunishmentManager;
    private final AntiBotCheck antiBotCheck;
    private final RaidCheck raidCheck;

    public PlayerListener(PacketInjector injector, ShadowFlaggingSystem flaggingSystem,
                          BanWaveExecutor banWaveExecutor, AppealCodeManager appealCodeManager,
                          ChatPunishmentManager chatPunishmentManager,
                          AntiBotCheck antiBotCheck, RaidCheck raidCheck) {
        this.injector = injector;
        this.flaggingSystem = flaggingSystem;
        this.banWaveExecutor = banWaveExecutor;
        this.appealCodeManager = appealCodeManager;
        this.chatPunishmentManager = chatPunishmentManager;
        this.antiBotCheck = antiBotCheck;
        this.raidCheck = raidCheck;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String ip = p.getAddress() instanceof InetSocketAddress addr ? addr.getAddress().getHostAddress() : null;

        if (antiBotCheck.checkJoin(p.getName(), ip)) {
            p.kickPlayer("Connection rejected: bot pattern detected");
            Bukkit.getLogger().warning("Watchcat rejected bot connection: " + p.getName() + " from " + ip);
            return;
        }

        if (raidCheck.checkJoin(p, ip)) {
            return;
        }

        if (chatPunishmentManager.checkJoinBan(p)) {
            p.kickPlayer("Still on cooldown from previous violations");
            return;
        }

        injector.inject(p);
        banWaveExecutor.checkPendingBan(p);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        injector.uninject(p);
        if (flaggingSystem.getScore(p.getUniqueId()) >= 0.5) {
            flaggingSystem.unload(p.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (chatPunishmentManager.isMuted(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            long remaining = chatPunishmentManager.getMuteRemaining(e.getPlayer().getUniqueId()) / 1000;
            e.getPlayer().sendMessage("You are muted for " + remaining + " more seconds.");
        }
    }
}
