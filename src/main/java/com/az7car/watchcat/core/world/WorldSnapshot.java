package com.az7car.watchcat.core.world;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

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
        final int minY;
        final int height;

        ChunkData(int x, int z, BlockState[] blocks, int minY, int height) {
            this.x = x;
            this.z = z;
            this.blocks = blocks;
            this.minY = minY;
            this.height = height;
        }
    }

    public static void capture(World world) {
        String key = world.getName();
        CraftWorld cw = (CraftWorld) world;
        var nmsWorld = cw.getHandle();
        Map<Long, ChunkData> chunkMap = new HashMap<>();
        int maxY = world.getMaxHeight();
        int minY = world.getMinHeight();
        int range = maxY - minY;
        for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
            if (chunk == null) continue;
            int cx = chunk.getX();
            int cz = chunk.getZ();
            BlockState[] states = new BlockState[16 * 16 * range];
            int i = 0;
            for (int bx = 0; bx < 16; bx++) {
                for (int bz = 0; bz < 16; bz++) {
                    for (int by = minY; by < maxY; by++) {
                        var bukkitBlock = chunk.getBlock(bx, by, bz);
                        states[i] = ((org.bukkit.craftbukkit.block.CraftBlock) bukkitBlock).getNMS();
                        i++;
                    }
                }
            }
            long key2 = (long) cx << 32 | (cz & 0xFFFFFFFFL);
            chunkMap.put(key2, new ChunkData(cx, cz, states, minY, range));
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
        int idx = (x & 15) * 16 * cd.height + (z & 15) * cd.height + (y - cd.minY);
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
                    if (bs != null && !bs.isAir()) return true;
                }
            }
        }
        return false;
    }

    public static void cleanup() { snapshots.clear(); }
}
