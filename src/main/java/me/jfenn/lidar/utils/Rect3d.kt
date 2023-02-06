package me.jfenn.lidar.utils

import net.minecraft.util.math.Vec3d
import kotlin.math.abs

class Rect3d(
    val a: Vec3d,
    val b: Vec3d,
    val c: Vec3d,
    val d: Vec3d,
) {

    fun findIntersection(origin: Vec3d, direction: Vec3d): Vec3d? {
        val vecBA = b.subtract(a)
        val vecCA = c.subtract(a)

        val normal = (vecBA).crossProduct(vecCA)
        val denominator = normal.dotProduct(direction)
        if (abs(denominator) < 1e-6f) return null

        val t = -normal.dotProduct(origin.subtract(a)) / denominator
        if (t < 0.0) return null

        val m = origin.add(direction.multiply(t))
        val dMS1 = m.subtract(a)
        val u = dMS1.dotProduct(vecBA)
        val v = dMS1.dotProduct(vecCA)

        if (u >= 0f && u <= vecBA.dotProduct(vecBA) && v >= 0f && v <= vecCA.dotProduct(vecCA)) {
            // println(": $origin -> $direction, quad $a $b $c, found intersection with length $t")
            return m
        }

        return null
    }

}
