package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class FlightCheck extends AbstractCheck {

    private final double hoverThreshold;
    private final int hoverTicks;
    private final double maxVerticalAccel;
    private int currentHoverTicks;

    public FlightCheck(WatchcatConfig config) {
        super("Flight", "mod",
            config.getCheckWeight("mod.flight", 0.8),
            config.isCheckEnabled("mod.flight", true));
        this.hoverThreshold = config.getCheckDouble("mod.flight", "hover-threshold", 0.01);
        this.hoverTicks = (int) config.getCheckDouble("mod.flight", "hover-ticks", 10);
        this.maxVerticalAccel = config.getCheckDouble("mod.flight", "max-vertical-accel", 0.005);
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;
        if (player.getAllowFlight() || player.isFlying() || player.isInsideVehicle()) return CheckResult.PASS;
        if (player.getLocation().getBlock().isLiquid()) return CheckResult.PASS;

        double deltaY = data.getDeltaY();
        int airTicks = data.getAirTicks();

        if (airTicks > 3 && Math.abs(deltaY) < hoverThreshold) {
            currentHoverTicks++;
            if (currentHoverTicks > hoverTicks * 2) {
                return CheckResult.CANCELLED;
            }
        } else {
            currentHoverTicks = 0;
        }

        if (airTicks > 5) {
            double predicted = -0.08 * Math.pow(0.98, airTicks - 1);
            if (deltaY > predicted + 0.2 && deltaY > 0) {
                return CheckResult.CANCELLED;
            }
        }

        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) return CheckResult.PASS;
        if (player.getAllowFlight() || player.isFlying() || player.isInsideVehicle()) return CheckResult.PASS;
        if (player.getLocation().getBlock().isLiquid()) return CheckResult.PASS;

        double deltaY = data.getDeltaY();
        int airTicks = data.getAirTicks();

        if (airTicks > 3 && Math.abs(deltaY) < hoverThreshold) {
            currentHoverTicks++;
            if (currentHoverTicks > hoverTicks) {
                return CheckResult.FLAG;
            }
        } else {
            currentHoverTicks = 0;
        }

        if (airTicks > 5) {
            double predicted = -0.08 * Math.pow(0.98, airTicks - 1);
            if (deltaY > predicted + 0.15 && deltaY > 0) {
                return CheckResult.FLAG;
            }
        }

        return CheckResult.PASS;
    }
}
