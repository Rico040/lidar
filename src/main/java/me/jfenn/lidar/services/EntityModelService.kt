package me.jfenn.lidar.services

import me.jfenn.lidar.utils.Rect3d
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.boss.dragon.EnderDragonPart
import net.minecraft.util.math.Vec3d

object EntityModelService {

    class TriInterceptor(
        private val callback: (Rect3d) -> Unit
    ) : VertexConsumer {

        private val points = mutableListOf<Vec3d>()

        override fun vertex(x: Double, y: Double, z: Double): VertexConsumer = apply {
            points.add(Vec3d(x, y, z))

            if (points.size >= 4) { // every 4 vectors = 1 quad to return
                callback(Rect3d(points.removeFirst(), points.removeFirst(), points.removeFirst(), points.removeFirst()))
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

    class TriInterceptorProvider(
        callback: (Rect3d) -> Unit
    ) : VertexConsumerProvider {

        private val interceptor = TriInterceptor(callback)

        override fun getBuffer(layer: RenderLayer?): VertexConsumer
            = interceptor
    }

    fun getCollisionPoint(entity: Entity, origin: Vec3d, direction: Vec3d): Vec3d? {
        val dispatcher = MinecraftClient.getInstance().entityRenderDispatcher ?: return null
        var hit: Vec3d? = null

        dispatcher.render(entity, entity.x, entity.y, entity.z, entity.yaw, 0f, MatrixStack(), TriInterceptorProvider { rect ->
            rect.findIntersection(origin, direction)?.let {
                if ((hit?.distanceTo(origin) ?: Double.MAX_VALUE) > it.distanceTo(origin))
                    hit = it
            }
        }, 0)

        return hit?.takeIf {
            entity.boundingBox.contains(it) && it.y > entity.y
        }
    }



}