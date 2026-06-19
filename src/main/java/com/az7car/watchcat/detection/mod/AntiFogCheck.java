package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class AntiFogCheck extends AbstractCheck {

    private int antiFogCount;
    private boolean wasInPowderSnow;

    public AntiFogCheck(WatchcatConfig config) {
        super("AntiFog", "mod",
            config.getCheckWeight("mod.antifog", 0.3),
            config.isCheckEnabled("mod.antifog", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        boolean inPowderSnow = player.getLocation().getBlock().getType().name().contains("POWDER_SNOW");
        boolean inLava = player.isInLava();
        if (inPowderSnow || inLava) {
            wasInPowderSnow = true;
        }
        if (wasInPowderSnow && !inPowderSnow && !inLava) {
            float hv = (float) data.getHorizontalVelocity();
            if (hv > 0.15f) {
                antiFogCount++;
                if (antiFogCount > 3) {
                    return CheckResult.FLAG;
                }
            }
            wasInPowderSnow = false;
        }
        return CheckResult.PASS;
    }
}
