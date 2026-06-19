package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import java.util.Set;

public class AntiWDLCheck extends AbstractCheck {

    private int antiWDLCount;
    private static final Set<String> WDL_PAYLOADS = Set.of(
        "wdl", "worlddownloader", "WDL", "WDL|INIT",
        "worlddownloader:init", "wdl:init"
    );

    public AntiWDLCheck(WatchcatConfig config) {
        super("AntiWDL", "mod",
            config.getCheckWeight("mod.antiwdl", 0.5),
            config.isCheckEnabled("mod.antiwdl", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        String brand = data.getClientBrand();
        if (brand != null) {
            String lower = brand.toLowerCase();
            if (lower.contains("worlddownloader") || lower.contains("wdl")) {
                antiWDLCount++;
                if (antiWDLCount > 1) return CheckResult.FLAG;
            }
        }
        if (antiWDLCount > 0) antiWDLCount = Math.max(0, antiWDLCount - 1);
        return CheckResult.PASS;
    }
}
