package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
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

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundClientInformationPacket info)) return CheckResult.PASS;
        boolean currentSkin = info.isSkinCustomization();
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
        boolean currentSkin = info.isSkinCustomization();
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
