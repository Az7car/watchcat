package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class NoScoreboardCheck extends AbstractCheck {

    private int noScoreboardCount;

    public NoScoreboardCheck(WatchcatConfig config) {
        super("NoScoreboard", "mod",
            config.getCheckWeight("mod.noscoreboard", 0.3),
            config.isCheckEnabled("mod.noscoreboard", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        Scoreboard sb = player.getScoreboard();
        var objectives = sb.getObjectives();
        int objCount = 0;
        for (var o : objectives) objCount++;
        if (objCount > 0) {
            boolean hasScoreboard = player.getScoreboard().getObjective("sidebar") != null
                || player.getScoreboard().getObjective("list") != null;
            if (!hasScoreboard) {
                noScoreboardCount++;
                if (noScoreboardCount > 5) {
                    return CheckResult.FLAG;
                }
            }
        }
        if (noScoreboardCount > 0) {
            noScoreboardCount = Math.max(0, noScoreboardCount - 1);
        }
        return CheckResult.PASS;
    }
}
