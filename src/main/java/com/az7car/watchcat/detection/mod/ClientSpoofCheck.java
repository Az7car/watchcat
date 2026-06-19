package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class ClientSpoofCheck extends AbstractCheck {

    private int spoofCount;

    public ClientSpoofCheck(WatchcatConfig config) {
        super("ClientSpoof", "mod",
            config.getCheckWeight("mod.clientspoof", 0.6),
            config.isCheckEnabled("mod.clientspoof", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof ServerboundCustomPayloadPacket payload)) return CheckResult.PASS;
        String channel;
        try {
            channel = payload.getName();
        } catch (Exception e) { return CheckResult.PASS; }
        if (channel == null) return CheckResult.PASS;

        String brand = data.getClientBrand();
        if (brand != null && !brand.isEmpty()) {
            String lower = brand.toLowerCase();
            boolean hasVanilla = lower.contains("vanilla");
            boolean hasFabric = lower.contains("fabric") || lower.contains("fapi");
            boolean hasForge = lower.contains("forge") || lower.contains("fml");
            int legitCount = 0;
            if (hasVanilla) legitCount++;
            if (hasFabric) legitCount++;
            if (hasForge) legitCount++;

            if (legitCount > 1) {
                spoofCount++;
                if (spoofCount > 3) {
                    return CheckResult.FLAG;
                }
            } else {
                spoofCount = Math.max(0, spoofCount - 1);
            }
        } else {
            spoofCount = Math.max(0, spoofCount - 1);
        }
        return CheckResult.PASS;
    }
}
