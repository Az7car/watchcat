package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class NoWeatherCheck extends AbstractCheck {

    private int noWeatherCount;
    private boolean playerSetWeather;

    public NoWeatherCheck(WatchcatConfig config) {
        super("NoWeather", "mod",
            config.getCheckWeight("mod.noweather", 0.3),
            config.isCheckEnabled("mod.noweather", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!player.getWorld().hasStorm()) {
            noWeatherCount = Math.max(0, noWeatherCount - 1);
            return CheckResult.PASS;
        }
        boolean playerClear = player.getPlayerWeather() == org.bukkit.WeatherType.CLEAR
            && player.getWorld().hasStorm();
        if (playerClear && player.getPlayerTime() != player.getWorld().getFullTime()) {
            noWeatherCount++;
            if (noWeatherCount > 3) {
                return CheckResult.FLAG;
            }
        } else {
            noWeatherCount = Math.max(0, noWeatherCount - 1);
        }
        return CheckResult.PASS;
    }
}
