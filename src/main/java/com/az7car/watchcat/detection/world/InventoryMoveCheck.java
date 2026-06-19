package com.az7car.watchcat.detection.world;

import com.az7car.watchcat.core.config.WatchcatConfig;
import com.az7car.watchcat.core.pipeline.AbstractCheck;
import com.az7car.watchcat.detection.base.CheckResult;
import com.az7car.watchcat.detection.base.PlayerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Player;

public class InventoryMoveCheck extends AbstractCheck {

    public InventoryMoveCheck(WatchcatConfig config) {
        super("InventoryMove", "world",
            config.getCheckWeight("world.inventorymove"),
            config.isCheckEnabled("world.inventorymove"));
    }

    @Override
    public CheckResult processSync(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        if (!(packet instanceof net.minecraft.network.protocol.game.ServerboundMovePlayerPacket)) return CheckResult.PASS;
        boolean hasOpenInv = player.getOpenInventory() != null
            && !player.getOpenInventory().getTitle().equals("container.crafting");
        if (hasOpenInv) {
            double horizontal = data.getHorizontalPositionDelta();
            double vertical = Math.abs(data.getDeltaY());
            if (horizontal > 0.1 || vertical > 0.1) {
                return CheckResult.CANCELLED;
            }
        }
        return CheckResult.PASS;
    }

    @Override
    public CheckResult process(Player player, PlayerData data, Packet<?> packet, ServerPlayer nmsPlayer) {
        boolean hasOpenInv = player.getOpenInventory() != null
            && !player.getOpenInventory().getTitle().equals("container.crafting");

        data.setInventoryOpen(hasOpenInv);

        if (hasOpenInv && hasMovement(packet)) {
            return CheckResult.FLAG;
        }

        return CheckResult.PASS;
    }

    private boolean hasMovement(Packet<?> p) {
        return p instanceof net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
            || p instanceof net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos
            || p instanceof net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot
            || p instanceof net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot
            || p instanceof net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
    }
}
