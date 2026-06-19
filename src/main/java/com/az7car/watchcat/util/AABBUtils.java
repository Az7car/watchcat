package com.az7car.watchcat.util;

import org.bukkit.util.Vector;

public final class AABBUtils {

    private AABBUtils() {}

    public static class AABB {
        public final double minX, minY, minZ, maxX, maxY, maxZ;

        public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX; this.minY = minY; this.minZ = minZ;
            this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        }

        public AABB expand(double x, double y, double z) {
            return new AABB(minX - x, minY - y, minZ - z, maxX + x, maxY + y, maxZ + z);
        }
    }

    public static AABB getEntityAABB(org.bukkit.entity.Entity entity) {
        var loc = entity.getLocation();
        double w = 0.6 / 2;
        double h = entity.getHeight();
        return new AABB(
            loc.getX() - w, loc.getY(), loc.getZ() - w,
            loc.getX() + w, loc.getY() + h, loc.getZ() + w
        );
    }

    public static AABB fromNmsEntity(net.minecraft.world.entity.Entity nmsEntity) {
        var bb = nmsEntity.getBoundingBox();
        return new AABB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    public static double rayIntersectsAABB(Vector origin, Vector direction, AABB aabb) {
        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;

        for (int i = 0; i < 3; i++) {
            double originComp = i == 0 ? origin.getX() : i == 1 ? origin.getY() : origin.getZ();
            double dirComp = i == 0 ? direction.getX() : i == 1 ? direction.getY() : direction.getZ();
            double minComp = i == 0 ? aabb.minX : i == 1 ? aabb.minY : aabb.minZ;
            double maxComp = i == 0 ? aabb.maxX : i == 1 ? aabb.maxY : aabb.maxZ;

            if (Math.abs(dirComp) < 1e-7) {
                if (originComp < minComp || originComp > maxComp) return -1;
                continue;
            }

            double t1 = (minComp - originComp) / dirComp;
            double t2 = (maxComp - originComp) / dirComp;

            if (t1 > t2) { double t = t1; t1 = t2; t2 = t; }
            if (t1 > tMin) tMin = t1;
            if (t2 < tMax) tMax = t2;

            if (tMin > tMax) return -1;
        }

        return tMin;
    }

    public static double distanceToAABB(Vector point, AABB aabb) {
        double dx = Math.max(aabb.minX - point.getX(), Math.max(0, point.getX() - aabb.maxX));
        double dy = Math.max(aabb.minY - point.getY(), Math.max(0, point.getY() - aabb.maxY));
        double dz = Math.max(aabb.minZ - point.getZ(), Math.max(0, point.getZ() - aabb.maxZ));
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static Vector closestPointOnAABB(Vector point, AABB aabb) {
        double x = MathUtils.clamp(point.getX(), aabb.minX, aabb.maxX);
        double y = MathUtils.clamp(point.getY(), aabb.minY, aabb.maxY);
        double z = MathUtils.clamp(point.getZ(), aabb.minZ, aabb.maxZ);
        return new Vector(x, y, z);
    }
}
