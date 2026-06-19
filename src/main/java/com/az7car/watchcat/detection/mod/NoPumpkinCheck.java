package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoPumpkinCheck extends AbstractCheck {

    private int noPumpkinCount;
    private boolean hadPumpkin;

    public NoPumpkinCheck(WatchcatConfig config) {
        super("NoPumpkin", "mod",
            config.getCheckWeight("mod.nopumpkin", 0.35),
            config.isCheckEnabled("mod.nopumpkin", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        var helmet = player.getInventory().getHelmet();
        boolean hasPumpkin = helmet != null && helmet.getType().name().equals("CARVED_PUMPKIN");
        if (hasPumpkin) {
            hadPumpkin = true;
        }
        if (hadPumpkin && !hasPumpkin) {
            noPumpkinCount++;
            if (noPumpkinCount > 2) {
                return CheckResult.FLAG;
            }
            hadPumpkin = false;
        }
        return CheckResult.PASS;
    }
}
