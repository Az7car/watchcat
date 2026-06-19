package com.az7car.watchcat.detection.mod;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;
import java.net.InetSocketAddress;

public class ProxyCheck extends AbstractCheck {

    private static final java.util.List<String> SUSPICIOUS_ASNS = java.util.Arrays.asList(
        "OVH", "HETZNER", "DIGITALOCEAN", "AMAZON", "GOOGLE", "MICROSOFT",
        "CONTABO", "SOYOUSTART", "KILN", "CHOOPA", "MELBICLOUD"
    );

    private int proxyCount;

    public ProxyCheck(WatchcatConfig config) {
        super("Proxy", "mod",
            config.getCheckWeight("mod.proxy", 0.4),
            config.isCheckEnabled("mod.proxy", true));
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        try {
            InetSocketAddress addr = (InetSocketAddress) player.getAddress();
            if (addr != null) {
                String host = addr.getHostString();
                if (host != null && !host.isEmpty() && !host.equals("127.0.0.1")) {
                    for (String asn : SUSPICIOUS_ASNS) {
                        if (host.toLowerCase().contains(asn.toLowerCase())) {
                            proxyCount++;
                            if (proxyCount > 3) {
                                return CheckResult.FLAG;
                            }
                            return CheckResult.PASS;
                        }
                    }
                }
            }
        } catch (Exception e) {}
        proxyCount = Math.max(0, proxyCount - 1);
        return CheckResult.PASS;
    }
}
