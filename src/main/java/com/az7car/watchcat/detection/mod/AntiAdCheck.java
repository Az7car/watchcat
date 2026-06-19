package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import com.az7car.watchcat.punishment.ChatPunishmentManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class AntiAdCheck extends AbstractCheck {

    private final ChatPunishmentManager punishmentManager;
    private final boolean blockIpAddresses;
    private final boolean blockDomains;
    private final boolean blockInvites;

    private static final Pattern IP_PATTERN = Pattern.compile(
        "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");

    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
        "\\b(?:https?://|www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z]{2,}(?:\\.[a-zA-Z]{2,})?(?:/[-a-zA-Z0-9@:%_+.~#?&/=]*)?\\b",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern DISCORD_INVITE = Pattern.compile(
        "(?:discord\\.(?:gg|io|me|com/invite))/[a-zA-Z0-9_-]+",
        Pattern.CASE_INSENSITIVE);

    private static final java.util.List<String> ALLOWED_DOMAINS = java.util.Arrays.asList(
        "amethystcore.com", "orvexsmp.net"
    );

    public AntiAdCheck(WatchcatConfig config, ChatPunishmentManager punishmentManager) {
        super("AntiAd", "mod",
            config.getCheckWeight("mod.antiad", 0.5),
            config.isCheckEnabled("mod.antiad", true));
        this.punishmentManager = punishmentManager;
        this.blockIpAddresses = config.getBoolean("chat.block-ip-addresses", true);
        this.blockDomains = config.getBoolean("chat.block-domains", true);
        this.blockInvites = config.getBoolean("chat.block-invites", true);
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundChatPacket chat)) return CheckResult.PASS;

        String message;
        try { message = (String) chat.getClass().getMethod("message").invoke(chat); }
        catch (Exception e) { return CheckResult.PASS; }
        if (message == null || message.isEmpty()) return CheckResult.PASS;

        if (blockIpAddresses && IP_PATTERN.matcher(message).find()) {
            ChatPunishmentManager.Result r = punishmentManager.warnPlayer(player,
                "IP addresses not allowed in chat");
            return r == ChatPunishmentManager.Result.BANNED ? CheckResult.FAIL : CheckResult.FLAG;
        }

        if (blockInvites && DISCORD_INVITE.matcher(message).find()) {
            ChatPunishmentManager.Result r = punishmentManager.warnPlayer(player,
                "Server invites not allowed");
            return r == ChatPunishmentManager.Result.BANNED ? CheckResult.FAIL : CheckResult.FLAG;
        }

        if (blockDomains) {
            java.util.regex.Matcher matcher = DOMAIN_PATTERN.matcher(message);
            if (matcher.find()) {
                String domain = matcher.group();
                String clean = domain.toLowerCase()
                    .replaceAll("https?://", "")
                    .replaceAll("www\\.", "");
                boolean allowed = false;
                for (String a : ALLOWED_DOMAINS) {
                    if (clean.contains(a)) { allowed = true; break; }
                }
                if (!allowed) {
                    ChatPunishmentManager.Result r = punishmentManager.warnPlayer(player,
                        "External links not allowed");
                    return r == ChatPunishmentManager.Result.BANNED ? CheckResult.FAIL : CheckResult.FLAG;
                }
            }
        }

        return CheckResult.PASS;
    }
}
