package me.jfenn.lidar.services

import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f
import net.minecraft.util.math.Vector4f
import net.minecraft.util.math.random.Random
import java.lang.reflect.Field
import kotlin.math.abs

object EntityModelService {

    const val MAX_CAST = 3.0;

    class TriInterceptor(
        private val callback: (Vec3d, Vec3d, Vec3d, Vec3d) -> Unit
    ) : VertexConsumer {

        private val points = mutableListOf<Vec3d>()

        override fun vertex(x: Double, y: Double, z: Double): VertexConsumer = apply {
            points.add(Vec3d(x, y, z))

            if (points.size >= 4) { // every 4 vectors = 1 quad to return
                callback(points.removeFirst(), points.removeFirst(), points.removeFirst(), points.removeFirst())
            }
        }

        override fun color(red: Int, green: Int, blue: Int, alpha: Int): VertexConsumer = this
        override fun texture(u: Float, v: Float): VertexConsumer = this
        override fun overlay(u: Int, v: Int): VertexConsumer = this
        override fun light(u: Int, v: Int): VertexConsumer = this
        override fun normal(x: Float, y: Float, z: Float): VertexConsumer = this
        override fun next() {}
        override fun fixedColor(red: Int, green: Int, blue: Int, alpha: Int) {}
        override fun unfixColor() {}
    }

    private fun Class<*>.javaFields() : List<Field> {
        var current: Class<*> = this
        val fields = mutableListOf<Field>()

        while (current.superclass != Object::class.java) {
            fields.addAll(current.declaredFields)
            current = current.superclass
        }

        return fields
    }

    private fun Vec3d.transformBy(matrix4f: Matrix4f) =
        Vector4f(x.toFloat(), y.toFloat(), z.toFloat(), 1f)
            .also { it.transform(matrix4f) }
            .let { Vec3d(it.x.toDouble(), it.y.toDouble(), it.z.toDouble()) }

    fun getCollisionPoint(entity: LivingEntity, origHit: Vec3d, direction: Vec3d): Vec3d? {
        val dispatcher = MinecraftClient.getInstance().entityRenderDispatcher ?: return null
        val renderer = MinecraftClient.getInstance().entityRenderDispatcher.getRenderer(entity) ?: return null
        if (renderer !is LivingEntityRenderer<*, *>) return null

        //println("casting onto ${entity.type}, from $origHit -> $direction")

        var minHitLen: Double? = null

        dispatcher.render(entity, entity.x, entity.y, entity.z, entity.yaw, 0f, MatrixStack(), {
            TriInterceptor { a, b, c, d ->
                //println(": checking quad $a $b $c")
                val vecBA = b.subtract(a)
                val vecCA = c.subtract(a)

                val normal = (vecBA).crossProduct(vecCA)
                val denominator = normal.dotProduct(direction)
                if (abs(denominator) < 1e-6f) return@TriInterceptor

                val t = normal.dotProduct(origHit.subtract(a)) / denominator
                if (t < 0.0) return@TriInterceptor

                val m = origHit.add(direction.multiply(t))
                val dMS1 = m.subtract(a)
                val u = dMS1.dotProduct(vecBA)
                val v = dMS1.dotProduct(vecCA)

                if (u >= 0f && u <= vecBA.length() && v >= 0f && v <= vecCA.length()) {
                    println(": $origHit -> $direction, quad $a $b $c, found intersection with length $t")
                    if (t < (minHitLen ?: Double.MAX_VALUE))
                        minHitLen = t
                }
            }
        }, 0)

        return minHitLen?.let {
            origHit.add(direction.multiply(it))
        }
    }

    /*
    fun getModelCollision(entity: LivingEntity, origHit: Vec3d, direction: Vec3d): Vec3d? {
        val parts = renderer.model.javaClass.javaFields()
            .filter { ModelPart::class.java.isAssignableFrom(it.type) }
            .mapNotNull {
                if (it.trySetAccessible())
                    it.get(renderer.model) as? ModelPart
                else null
            }

        // Transform matrix stack according to EntityRenderDispatcher
        matrix.translate(entity.x, entity.y, entity.z)
        matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f - entity.bodyYaw))
        // Transform matrix stack according to LivingEntityRenderer
        // TODO: entity.setupTransforms
        matrix.translate(0.0, -1.5010000467300415, 0.0)
        // TODO: entity.scale

        var closestDist: Double? = null
        var closestBox: Box? = null
        var closestCuboid: ModelPart.Cuboid? = null
        var closestMatrix: Matrix4f? = null
        var closestIndex: Int? = null
        parts.forEachIndexed { index, part ->
            part.forEachCuboid(matrix) { matrixEntry, path, _, cuboid ->
                val cuboidBox = with(cuboid) {
                    Box(
                        minX.toDouble(), minY.toDouble(), minZ.toDouble(),
                        maxX.toDouble(), maxY.toDouble(), maxZ.toDouble()
                    )
                }
                val cuboidCenter = cuboidBox.center.transformBy(matrixEntry.positionMatrix)
                    .also { println("cuboid centered on $it" )}

                val cuboidDist = origHit.distanceTo(cuboidCenter)
                if (cuboidDist <= (closestDist ?: Double.MAX_VALUE)) {
                    closestDist = cuboidDist
                    closestBox = cuboidBox
                    closestCuboid = cuboid
                    closestMatrix = matrixEntry.positionMatrix
                    closestIndex = index
                }
            }
        }

        if (closestCuboid != null && closestMatrix != null && closestIndex != null) {
            // transform coords from absolute to entity-relative position
            val relativeMatrix = closestMatrix!!.copy().also { it.invert() }

            // raycast from the hitbox location to the entity center
            println("transforming $origHit into...")
            val relativeHit = origHit.transformBy(relativeMatrix)
            println("raycasting from $relativeHit to ${closestBox!!.center}")
            val result = closestBox!!.raycast(relativeHit, Vec3d.ZERO)

            if (result.isPresent) {
                println("Collided with part $closestIndex on entity ${entity.type}")
                return result.get()
                    .also { println(": return relative $it") }
                    .transformBy(closestMatrix!!)
                    .also { println(": return absolute $it") }
            }
        }

        return null
    }
    */



}