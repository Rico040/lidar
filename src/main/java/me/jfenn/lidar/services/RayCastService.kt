package me.jfenn.lidar.services

import me.jfenn.lidar.Lidar.config
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import kotlin.math.pow


object RayCastService {

    fun getEntityProjections(entity: Entity, spreadDeg: Float, count: Int): List<Projection> {
        val spread = Math.toRadians(spreadDeg.toDouble())
        val vec = entity.getRotationVec(0f)
        return buildList(count) {
            for (i in 0..count) {
                val projection = vec.rotateX((spread * (Math.random() - 0.5)).toFloat())
                    .rotateY((spread * (Math.random() - 0.5)).toFloat())
                    .rotateZ((spread * (Math.random() - 0.5)).toFloat())

                add(Projection(entity.eyePos, projection).also {
                    raycastProjection(entity, it)
                })
            }
        }
    }

    fun raycastProjection(entity: Entity, projection: Projection) {
        val reachDistance = config.lidarDistance

        // raycast to find a visual block intersection
        projection.blockHit = entity.world?.raycast(RaycastContext(
            projection.origin,
            projection.origin.add(projection.direction.multiply(reachDistance)),
            RaycastContext.ShapeType.VISUAL,
            if (entity.isSubmergedInWater) RaycastContext.FluidHandling.NONE else RaycastContext.FluidHandling.ANY,
            entity,
        ))
        val blockDistanceSq = projection.blockHit?.pos?.squaredDistanceTo(projection.origin) ?: reachDistance.pow(2)

        val vec3d3 = projection.origin.add(projection.direction.multiply(blockDistanceSq))
        val box = entity.boundingBox.stretch(entity.getRotationVec(1.0f).multiply(reachDistance))
            .expand(1.0, 1.0, 1.0)

        // raycast to find an entity hitbox intersection
        projection.entityHit = ProjectileUtil.raycast(
            entity,
            projection.origin,
            vec3d3,
            box,
            { entityx: Entity -> !entityx.isSpectator && entityx.canHit() },
            blockDistanceSq
        )?.takeIf {
            // only return the entity if entity hit is closer than block
            it.pos.squaredDistanceTo(projection.origin) <= blockDistanceSq
        }
    }

    class Projection(
        val origin: Vec3d,
        val direction: Vec3d,
        var blockHit: BlockHitResult? = null,
        var entityHit: EntityHitResult? = null,
        var entityHitPos: Vec3d? = null,
    )

}