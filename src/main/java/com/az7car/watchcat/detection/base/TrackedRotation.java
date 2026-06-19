package com.az7car.watchcat.detection.base;

public class TrackedRotation {
    private final float yaw;
    private final float pitch;
    private final float deltaYaw;
    private final float deltaPitch;
    private final long timestamp;

    public TrackedRotation(float yaw, float pitch, float deltaYaw, float deltaPitch, long timestamp) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.deltaYaw = deltaYaw;
        this.deltaPitch = deltaPitch;
        this.timestamp = timestamp;
    }

    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public float deltaYaw() { return deltaYaw; }
    public float deltaPitch() { return deltaPitch; }
    public long timestamp() { return timestamp; }
}
