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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class AntiSwearCheck extends AbstractCheck {

    private final ChatPunishmentManager punishmentManager;

    private static final List<Pattern> SWEAR_PATTERNS = Arrays.asList(
        Pattern.compile("\\bfuck(?:ing|er|ed|off|you)?\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bshit(?:ty|head|hole)?\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bbitch(?:es|ing|y)?\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bass(?:hole|hat)?\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bcunt\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bnigg(?:er|a|ah)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bdick(?:head|bag)?\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bpussy\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bwhore\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bslut\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bretard(?:ed)?\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bspic\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bchink\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bgook\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bwop\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bkike\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bfag(?:got|g)?\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bnazi\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bkkk\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bcock\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bdouche(?:bag)?\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\btwat\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bknob\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bwank(?:er|ing)?\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bbastard\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bprick\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\btits?\\b", Pattern.CASE_INSENSITIVE)
    );

    public AntiSwearCheck(WatchcatConfig config, ChatPunishmentManager punishmentManager) {
        super("AntiSwear", "mod",
            config.getCheckWeight("mod.antiswear", 0.5),
            config.isCheckEnabled("mod.antiswear", true));
        this.punishmentManager = punishmentManager;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundChatPacket chat)) return CheckResult.PASS;

        String message = chat.getMessage();
        if (message == null || message.isEmpty()) return CheckResult.PASS;

        for (Pattern pattern : SWEAR_PATTERNS) {
            if (pattern.matcher(message).find()) {
                ChatPunishmentManager.Result result = punishmentManager.warnPlayer(player,
                    "Inappropriate language detected");
                if (result == ChatPunishmentManager.Result.MUTED) {
                    return CheckResult.FAIL;
                }
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
