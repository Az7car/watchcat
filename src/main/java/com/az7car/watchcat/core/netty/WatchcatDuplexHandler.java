package com.az7car.watchcat.core.netty;

import com.az7car.watchcat.WatchcatPlugin;
import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.core.pipeline.CheckExecutor;
import com.az7car.watchcat.core.pipeline.CheckRegistry;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class WatchcatDuplexHandler extends ChannelDuplexHandler {

    private static final String HANDLER_NAME = "watchcat_duplex";

    private final WatchcatPlugin plugin;
    private final PacketProcessor processor;
    private final CheckRegistry registry;
    private final ServerPlayer nmsPlayer;
    private final Player bukkitPlayer;
    private final Logger logger;
    private final CheckExecutor checkExecutor;

    public WatchcatDuplexHandler(WatchcatPlugin plugin, PacketProcessor processor,
                                  CheckRegistry registry, Player player) {
        this.plugin = plugin;
        this.processor = processor;
        this.registry = registry;
        this.bukkitPlayer = player;
        this.nmsPlayer = ((CraftPlayer) player).getHandle();
        this.logger = plugin.getLogger();
        this.checkExecutor = new CheckExecutor();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Packet<?> packet) {
            try {
                PlayerData data = PlayerData.getOrCreate(bukkitPlayer);

                if (packet instanceof ServerboundMovePlayerPacket
                        || packet instanceof ServerboundMovePlayerPacket.Pos
                        || packet instanceof ServerboundMovePlayerPacket.PosRot
                        || packet instanceof ServerboundMovePlayerPacket.Rot
                        || packet instanceof ServerboundPlayerInputPacket) {
                    data.updatePosition(nmsPlayer);
                    if (runSyncChecks(data, packet, "movement")) {
                        return;
                    }
                    processor.enqueueMovement(bukkitPlayer, nmsPlayer, packet, registry);
                } else if (packet instanceof ServerboundInteractPacket
                        || packet instanceof ServerboundSwingPacket) {
                    if (runSyncChecks(data, packet, "combat")) {
                        return;
                    }
                    processor.enqueueCombat(bukkitPlayer, nmsPlayer, packet, registry);
                } else if (packet instanceof ServerboundUseItemOnPacket
                        || packet instanceof ServerboundUseItemPacket
                        || packet instanceof ServerboundSetCreativeModeSlotPacket
                        || packet instanceof ServerboundContainerClickPacket
                        || packet instanceof ServerboundPlaceRecipePacket) {
                    if (runSyncChecks(data, packet, "world")) {
                        return;
                    }
                    processor.enqueueWorld(bukkitPlayer, nmsPlayer, packet, registry);
                } else if (packet instanceof ServerboundCustomPayloadPacket
                        || packet instanceof ServerboundClientInformationPacket) {
                    processor.enqueueMod(bukkitPlayer, nmsPlayer, packet, registry);
                } else if (packet instanceof ServerboundAcceptTeleportationPacket
                        || packet instanceof ServerboundCommandSuggestionPacket
                        || packet instanceof ServerboundChatPacket) {
                    processor.enqueuePacket(bukkitPlayer, nmsPlayer, packet, registry);
                }
            } catch (Exception e) {
                if (plugin.getWatchcatConfig().isDebugMode()) {
                    logger.warning("Watchcat packet error: " + e.getMessage());
                }
            }
        }
        super.channelRead(ctx, msg);
    }

    private boolean runSyncChecks(PlayerData data, Packet<?> packet, String category) {
        var checks = switch (category) {
            case "combat" -> registry.getCombatChecks();
            case "movement" -> registry.getMovementChecks();
            case "world" -> registry.getWorldChecks();
            default -> registry.getModChecks();
        };

        for (AbstractCheck check : checks) {
            if (!check.isEnabled()) continue;
            try {
                CheckResult result = checkExecutor.executeSync(check, bukkitPlayer, data, packet, nmsPlayer);
                if (result == CheckResult.CANCELLED) {
                    data.cancelledPackets++;
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) {
        super.write(ctx, msg, promise);
    }

    public static void inject(Player player, WatchcatPlugin plugin,
                               PacketProcessor processor, CheckRegistry registry) {
        try {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            ServerGamePacketListenerImpl connection = craftPlayer.getHandle().connection;
            java.lang.reflect.Field connectionField = ServerGamePacketListenerImpl.class.getField("connection");
            Object networkManager = connectionField.get(connection);
            java.lang.reflect.Field channelField = networkManager.getClass().getField("channel");
            Channel channel = (Channel) channelField.get(networkManager);
            ChannelPipeline pipeline = channel.pipeline();

            if (pipeline.get(HANDLER_NAME) == null) {
                pipeline.addBefore("packet_handler", HANDLER_NAME,
                        new WatchcatDuplexHandler(plugin, processor, registry, player));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to inject Watchcat handler for " + player.getName() + ": " + e.getMessage());
        }
    }

    public static void uninject(Player player) {
        try {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            ServerGamePacketListenerImpl connection = craftPlayer.getHandle().connection;
            java.lang.reflect.Field connectionField = ServerGamePacketListenerImpl.class.getField("connection");
            Object networkManager = connectionField.get(connection);
            java.lang.reflect.Field channelField = networkManager.getClass().getField("channel");
            Channel channel = (Channel) channelField.get(networkManager);
            ChannelPipeline pipeline = channel.pipeline();
            if (pipeline.get(HANDLER_NAME) != null) {
                pipeline.remove(HANDLER_NAME);
            }
        } catch (Exception e) {
        }
    }
}
