package com.az7car.watchcat.core.world;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class WorldSnapshot {

    private static final ConcurrentHashMap<String, AtomicReference<Snapshot>> snapshots = new ConcurrentHashMap<>();
    private static final long SNAPSHOT_TTL_MS = 50;

    private static class Snapshot {
        final long time;
        final Map<Long, ChunkData> chunks;

        Snapshot(Map<Long, ChunkData> chunks) {
            this.time = System.currentTimeMillis();
            this.chunks = chunks;
        }

        boolean isStale() { return System.currentTimeMillis() - time > SNAPSHOT_TTL_MS; }
    }

    private static class ChunkData {
        final int x, z;
        final BlockState[] blocks;

        ChunkData(int x, int z, BlockState[] blocks) {
            this.x = x;
            this.z = z;
            this.blocks = blocks;
        }
    }

    public static void capture(World world) {
        String key = world.getName();
        CraftWorld cw = (CraftWorld) world;
        var nmsWorld = cw.getHandle();
        Map<Long, ChunkData> chunkMap = new HashMap<>();
        for (var chunk : nmsWorld.chunkSource().getChunks()) {
            if (chunk == null) continue;
            int cx = chunk.getPos().x;
            int cz = chunk.getPos().z;
            BlockState[] states = new BlockState[16 * 16 * chunk.getHeight()];
            int i = 0;
            for (int bx = 0; bx < 16; bx++) {
                for (int bz = 0; bz < 16; bz++) {
                    for (int by = chunk.getMinBuildHeight(); by < chunk.getMaxBuildHeight(); by++) {
                        states[i++] = chunk.getBlockState(bx, by, bz);
                    }
                }
            }
            long key2 = (long) cx << 32 | (cz & 0xFFFFFFFFL);
            chunkMap.put(key2, new ChunkData(cx, cz, states));
        }
        snapshots.compute(key, (k, old) -> {
            if (old != null && !old.get().isStale()) return old;
            return new AtomicReference<>(new Snapshot(chunkMap));
        });
    }

    public static BlockState getBlockState(World world, int x, int y, int z) {
        String key = world.getName();
        var ref = snapshots.get(key);
        if (ref == null) return null;
        Snapshot s = ref.get();
        if (s == null || s.isStale()) return null;
        int cx = x >> 4;
        int cz = z >> 4;
        long ck = (long) cx << 32 | (cz & 0xFFFFFFFFL);
        ChunkData cd = s.chunks.get(ck);
        if (cd == null) return null;
        CraftWorld cw = (CraftWorld) world;
        int maxY = cw.getHandle().getMaxBuildHeight();
        int minY = cw.getHandle().getMinBuildHeight();
        int idx = (x & 15) * 16 * (maxY - minY) + (z & 15) * (maxY - minY) + (y - minY);
        if (idx < 0 || idx >= cd.blocks.length) return null;
        return cd.blocks[idx];
    }

    public static boolean isColliding(World world, AABB box) {
        int minX = (int) Math.floor(box.minX);
        int maxX = (int) Math.ceil(box.maxX);
        int minY = (int) Math.floor(box.minY);
        int maxY = (int) Math.ceil(box.maxY);
        int minZ = (int) Math.floor(box.minZ);
        int maxZ = (int) Math.ceil(box.maxZ);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockState bs = getBlockState(world, x, y, z);
                    if (bs != null && !bs.isAir()) {
                        var blockBox = bs.getCollisionShape(net.minecraft.world.level.Level.RANDOM, null);
                        if (blockBox != null && !blockBox.isEmpty()) return true;
                    }
                }
            }
        }
        return false;
    }

    public static void cleanup() { snapshots.clear(); }
}
