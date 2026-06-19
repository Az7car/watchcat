package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class SkinBlinkerCheck extends AbstractCheck {

    private boolean lastSkinFlag;
    private int toggleCount;
    private long lastToggleTime;
    private final int maxTogglesPerSecond;

    public SkinBlinkerCheck(WatchcatConfig config) {
        super("SkinBlinker", "mod",
            config.getCheckWeight("mod.skinblinker", 0.5),
            config.isCheckEnabled("mod.skinblinker", true));
        this.maxTogglesPerSecond = (int) config.getCheckDouble("mod.skinblinker", "max-toggles-per-second", 3);
    }

    private static boolean hasSkinCustomization(ServerboundClientInformationPacket pkt) {
        try {
            var m = pkt.getClass().getMethod("isSkinCustomization");
            return (boolean) m.invoke(pkt);
        } catch (Exception e) {
            try {
                var f = pkt.getClass().getDeclaredField("skinCustomization");
                f.setAccessible(true);
                Object val = f.get(pkt);
                if (val instanceof Boolean) return (Boolean) val;
                if (val instanceof Long) return ((Long) val & 1) == 1;
            } catch (Exception ex) {}
            return false;
        }
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundClientInformationPacket info)) return CheckResult.PASS;
        boolean currentSkin = hasSkinCustomization(info);
        if (currentSkin != lastSkinFlag && lastSkinFlag) {
            long now = System.currentTimeMillis();
            if (now - lastToggleTime < 1000) {
                toggleCount++;
                if (toggleCount > maxTogglesPerSecond * 2) {
                    return CheckResult.CANCELLED;
                }
            } else {
                toggleCount = 1;
            }
            lastToggleTime = now;
        }
        lastSkinFlag = currentSkin;
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundClientInformationPacket info)) return CheckResult.PASS;
        boolean currentSkin = hasSkinCustomization(info);
        if (currentSkin != lastSkinFlag && lastSkinFlag) {
            long now = System.currentTimeMillis();
            if (now - lastToggleTime < 1000) {
                toggleCount++;
                if (toggleCount > maxTogglesPerSecond) {
                    return CheckResult.FLAG;
                }
            } else {
                toggleCount = 1;
            }
            lastToggleTime = now;
        }
        lastSkinFlag = currentSkin;
        return CheckResult.PASS;
    }
}
