package me.jfenn.lidar.services

import me.jfenn.lidar.Lidar
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin


object RayCastService {

    private fun pitchYawToVec3d(pitchDeg: Double, yawDeg: Double): Vec3d {
        val pitch = Math.toRadians(pitchDeg)
        val yaw = Math.toRadians(yawDeg)
        val xzLen = cos(pitch)
        val x = xzLen * cos(yaw)
        val y = sin(pitch)
        val z = xzLen * sin(-yaw)
        return Vec3d(x, y, z)
    }

    fun getEntityFacingVector(entity: LivingEntity)
        = entity.getRotationVec(0.0f)

    fun getEntityProjections(entity: LivingEntity, spreadDeg: Float, count: Int): List<Vec3d> {
        val spread = Math.toRadians(spreadDeg.toDouble())
        val vec = entity.getRotationVec(0f)
        return buildList(count) {
            for (i in 0..count) {
                val projection = vec.rotateX((spread * (Math.random() - 0.5)).toFloat())
                    .rotateY((spread * (Math.random() - 0.5)).toFloat())

                add(projection)
            }
        }
    }

    fun raycastInDirection(entity: LivingEntity, orig: Vec3d, direction: Vec3d): HitResult? {
        val reachDistance = Lidar.config.lidarDistance

        // raycast to find a visual block intersection
        val blockHitResult = entity.world?.raycast(RaycastContext(
            orig,
            orig.add(direction.multiply(reachDistance)),
            RaycastContext.ShapeType.VISUAL,
            RaycastContext.FluidHandling.ANY,
            entity,
        ))
        val blockDistanceSq = blockHitResult?.pos?.squaredDistanceTo(orig) ?: reachDistance.pow(2)

        val vec3d3 = orig.add(direction.multiply(blockDistanceSq))
        val box = entity.boundingBox.stretch(entity.getRotationVec(1.0f).multiply(reachDistance))
            .expand(1.0, 1.0, 1.0)

        // raycast to find an entity hitbox intersection
        val entityHitResult = ProjectileUtil.raycast(
            entity,
            orig,
            vec3d3,
            box,
            { entityx: Entity -> !entityx.isSpectator && entityx.collides() },
            blockDistanceSq
        ) ?: return blockHitResult

        // if entity hit is closer than block, return the entity
        if (entityHitResult.pos.squaredDistanceTo(orig) <= blockDistanceSq) {
            return entityHitResult
        }

        return blockHitResult
    }

}