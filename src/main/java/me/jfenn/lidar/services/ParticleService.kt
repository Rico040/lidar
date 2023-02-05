package me.jfenn.lidar.services

import me.jfenn.lidar.Lidar
import me.jfenn.lidar.data.DotParticle
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import java.util.LinkedList

class ParticleService {

    fun getColor(id: String, map: Map<String, String?>, default: String? = null): Int? {
        val color = if (map.containsKey(id)) map[id] else default
        return color?.removePrefix("#")
            ?.toIntOrNull(16)
    }

    fun addHit(client: MinecraftClient, hit: HitResult) {
        val world = client.world ?: return;

        when (hit) {
            is BlockHitResult -> {
                val blockState = world.getBlockState(hit.blockPos) ?: return
                if (blockState.isAir) return

                val id = Registry.BLOCK.getId(blockState.block).toString()
                val color = getColor(id, Lidar.config.blockColorMap, Lidar.config.blockColorDefault) ?: return

                addParticle(world, hit.pos, color)
            }
            is EntityHitResult -> {
                val entity = hit.entity ?: return
                val id = Registry.ENTITY_TYPE.getId(entity.type).toString()
                val color = getColor(id, Lidar.config.entityColorMap, Lidar.config.entityColorDefault) ?: return

                addParticle(
                    world = world,
                    pos = hit.pos,
                    color = color,
                    entityId = entity.id,
                    entityOffset = entity.pos.subtract(hit.pos)
                )
            }
        }
    }

    fun addParticle(world: ClientWorld, pos: Vec3d, color: Int, entityId: Int? = null, entityOffset: Vec3d? = null) {
        val entityOffsetDouble = entityOffset?.let {
            (((it.x * 128).toInt().coerceIn(-128, 128) + 128) shl 16)
                .or(((it.y * 128).toInt().coerceIn(-128, 128) + 128) shl 8)
                .or((it.z * 128).toInt().coerceIn(-128, 128) + 128)
        }?.toDouble()

        world.addImportantParticle(
            DotParticle.DOT,
            pos.x, pos.y, pos.z,
            color.toDouble(),
            entityId?.toDouble() ?: Double.MAX_VALUE,
            entityOffsetDouble ?: 0.0
        )
    }

    fun tick() {

    }

}