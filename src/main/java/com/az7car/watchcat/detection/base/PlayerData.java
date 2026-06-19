package com.az7car.watchcat.detection.base;

import com.az7car.watchcat.ml.FeatureVector;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PlayerData {

    private static final ConcurrentHashMap<UUID, PlayerData> DATA_MAP = new ConcurrentHashMap<>();

    private final UUID uuid;
    private final String playerName;

    private double x, y, z, lastX, lastY, lastZ;
    private float yaw, pitch, lastYaw, lastPitch;
    private float deltaYaw, deltaPitch, lastDeltaYaw, lastDeltaPitch;
    private boolean onGround, lastOnGround;
    private long lastPacketTime;
    private int airTicks, groundTicks;
    private float lastGcd;

    private final Deque<TrackedRotation> rotationBuffer;
    private final Deque<Long> packetTimestamps;
    private final Deque<Double> reachSamples;
    private final Deque<Long> clickTimestamps;
    private final Deque<Double> velocities;
    private final List<Float[]> rotationDeltas;

    private double velocityX, velocityY, velocityZ;
    private float health;
    private boolean inventoryOpen;
    private boolean wasInLiquid;
    private boolean wasOnClimbable;
    private long lastAttackTime;
    private long lastBlockPlaceTime;
    public int cancelledPackets;
    private int swingCount;
    private int lastSwingCount;
    private double fallDistance;
    private boolean isSneaking;
    private boolean isSprinting;
    private boolean isBlocking;

    private String clientBrand;
    private boolean probed;
    private FeatureVector featureVector;
    private float mlAnomalyScore;

    private int tick;

    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.playerName = player.getName();
        this.rotationBuffer = new ConcurrentLinkedDeque<>();
        this.packetTimestamps = new ConcurrentLinkedDeque<>();
        this.reachSamples = new ConcurrentLinkedDeque<>();
        this.clickTimestamps = new ConcurrentLinkedDeque<>();
        this.velocities = new ConcurrentLinkedDeque<>();
        this.rotationDeltas = new ArrayList<>();
        this.featureVector = new FeatureVector();
        Location loc = player.getLocation();
        this.x = loc.getX(); this.y = loc.getY(); this.z = loc.getZ();
        this.lastX = x; this.lastY = y; this.lastZ = z;
        this.yaw = loc.getYaw(); this.pitch = loc.getPitch();
        this.lastYaw = yaw; this.lastPitch = pitch;
    }

    public void updatePosition(ServerPlayer nmsPlayer) {
        lastX = x; lastY = y; lastZ = z;
        x = nmsPlayer.getX(); y = nmsPlayer.getY(); z = nmsPlayer.getZ();
        lastOnGround = onGround;
        onGround = nmsPlayer.onGround();
        lastYaw = yaw; lastPitch = pitch;
        yaw = nmsPlayer.getYRot(); pitch = nmsPlayer.getXRot();
        lastDeltaYaw = deltaYaw; lastDeltaPitch = deltaPitch;
        deltaYaw = yaw - lastYaw; deltaPitch = pitch - lastPitch;
        lastPacketTime = System.nanoTime();
        velocityX = nmsPlayer.getDeltaMovement().x();
        velocityY = nmsPlayer.getDeltaMovement().y();
        velocityZ = nmsPlayer.getDeltaMovement().z();
        health = nmsPlayer.getHealth();
        fallDistance = nmsPlayer.fallDistance;
        isSneaking = nmsPlayer.isShiftKeyDown();
        isSprinting = nmsPlayer.isSprinting();
        isBlocking = nmsPlayer.isBlocking();

        trackRotation();
        trackPacket();
        tick++;
    }

    private void trackRotation() {
        rotationBuffer.addLast(new TrackedRotation(yaw, pitch, deltaYaw, deltaPitch, System.nanoTime()));
        if (rotationBuffer.size() > 40) rotationBuffer.pollFirst();
        rotationDeltas.add(new Float[]{deltaYaw, deltaPitch});
        if (rotationDeltas.size() > 40) rotationDeltas.remove(0);
    }

    private void trackPacket() {
        packetTimestamps.addLast(System.nanoTime());
        if (packetTimestamps.size() > 100) packetTimestamps.pollFirst();
    }

    public void recordPacket(long nanoTime) {
        packetTimestamps.addLast(nanoTime);
        if (packetTimestamps.size() > 100) packetTimestamps.pollFirst();
    }

    public void recordClick() {
        clickTimestamps.addLast(System.currentTimeMillis());
        if (clickTimestamps.size() > 50) clickTimestamps.pollFirst();
    }

    public void recordReach(double reach) {
        reachSamples.addLast(reach);
        if (reachSamples.size() > 50) reachSamples.pollFirst();
    }

    public void recordAttack() { lastAttackTime = System.currentTimeMillis(); swingCount++; }
    public void recordBlockPlace() { lastBlockPlaceTime = System.currentTimeMillis(); }

    public void setAirTicks(int ticks) { airTicks = onGround ? 0 : ticks; }
    public void incAirTicks() { if (!onGround) airTicks++; else airTicks = 0; }
    public void incGroundTicks() { if (onGround) groundTicks++; else groundTicks = 0; }

    // Getters
    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getLastX() { return lastX; }
    public double getLastY() { return lastY; }
    public double getLastZ() { return lastZ; }
    public float getDeltaX() { return (float)(x - lastX); }
    public float getDeltaY() { return (float)(y - lastY); }
    public float getDeltaZ() { return (float)(z - lastZ); }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getDeltaYaw() { return deltaYaw; }
    public float getDeltaPitch() { return deltaPitch; }
    public float getLastDeltaYaw() { return lastDeltaYaw; }
    public float getLastDeltaPitch() { return lastDeltaPitch; }
    public boolean isOnGround() { return onGround; }
    public boolean wasOnGround() { return lastOnGround; }
    public long getLastPacketTime() { return lastPacketTime; }
    public int getAirTicks() { return airTicks; }
    public int getGroundTicks() { return groundTicks; }
    public Deque<TrackedRotation> getRotationBuffer() { return rotationBuffer; }
    public Deque<Long> getPacketTimestamps() { return packetTimestamps; }
    public Deque<Double> getReachSamples() { return reachSamples; }
    public Deque<Long> getClickTimestamps() { return clickTimestamps; }
    public List<Float[]> getRotationDeltas() { return rotationDeltas; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public double getVelocityZ() { return velocityZ; }
    public float getHealth() { return health; }
    public boolean isInventoryOpen() { return inventoryOpen; }
    public void setInventoryOpen(boolean open) { this.inventoryOpen = open; }
    public boolean wasInLiquid() { return wasInLiquid; }
    public void setWasInLiquid(boolean v) { wasInLiquid = v; }
    public boolean wasOnClimbable() { return wasOnClimbable; }
    public void setWasOnClimbable(boolean v) { wasOnClimbable = v; }
    public long getLastAttackTime() { return lastAttackTime; }
    public long getLastBlockPlaceTime() { return lastBlockPlaceTime; }
    public int getSwingCount() { return swingCount; }
    public int getLastSwingCount() { return lastSwingCount; }
    public void setSwingCount(int c) { swingCount = c; }
    public double getFallDistance() { return fallDistance; }
    public boolean isSneaking() { return isSneaking; }
    public boolean isSprinting() { return isSprinting; }
    public boolean isBlocking() { return isBlocking; }
    public String getClientBrand() { return clientBrand; }
    public void setClientBrand(String b) { clientBrand = b; }
    public boolean isProbed() { return probed; }
    public void setProbed(boolean p) { probed = p; }
    public float getMlAnomalyScore() { return mlAnomalyScore; }
    public void setMlAnomalyScore(float s) { mlAnomalyScore = s; }
    public FeatureVector getFeatureVector() { return featureVector; }
    public int getTick() { return tick; }

    public double getHorizontalVelocity() {
        return Math.sqrt(velocityX * velocityX + velocityZ * velocityZ);
    }

    public double getPositionDelta() {
        return Math.sqrt(Math.pow(x - lastX, 2) + Math.pow(y - lastY, 2) + Math.pow(z - lastZ, 2));
    }

    public double getHorizontalPositionDelta() {
        return Math.sqrt(Math.pow(x - lastX, 2) + Math.pow(z - lastZ, 2));
    }

    public float computeGcd() {
        if (Math.abs(lastDeltaYaw) < 0.001f || Math.abs(deltaYaw) < 0.001f) return 1.0f;
        float gcd = gcd(Math.abs(deltaYaw), Math.abs(lastDeltaYaw));
        float gcdPitch = gcd(Math.abs(deltaPitch), Math.abs(lastDeltaPitch));
        lastGcd = Math.min(gcd, gcdPitch);
        return lastGcd;
    }

    private float gcd(float a, float b) {
        if (b < 0.001f) return a;
        return gcd(b, a % b);
    }

    public static PlayerData getOrCreate(Player player) {
        return DATA_MAP.computeIfAbsent(player.getUniqueId(), k -> new PlayerData(player));
    }

    public static PlayerData get(UUID uuid) {
        return DATA_MAP.get(uuid);
    }

    public static void remove(UUID uuid) {
        DATA_MAP.remove(uuid);
    }

    public static void cleanup() {
        DATA_MAP.clear();
    }
}
