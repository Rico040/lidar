package me.jfenn.lidar.services

import me.jfenn.lidar.Lidar
import me.jfenn.lidar.data.DotParticle
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.PiglinEntity
import net.minecraft.entity.passive.CowEntity
import net.minecraft.entity.passive.LlamaEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import java.util.LinkedList

object ParticleService {

    const val OFFSET_MIN = 0.0
    const val OFFSET_MAX = 0.99

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

                val offset = hit.pos.subtract(hit.blockPos.x.toDouble(), hit.blockPos.y.toDouble(), hit.blockPos.z.toDouble())

                val pos = Vec3d(
                    (hit.blockPos.x + offset.x.coerceIn(OFFSET_MIN, OFFSET_MAX)),
                    (hit.blockPos.y + offset.y.coerceIn(OFFSET_MIN, OFFSET_MAX)),
                    (hit.blockPos.z + offset.z.coerceIn(OFFSET_MIN, OFFSET_MAX)),
                )

                addParticle(world, pos, DotParticle.Info(color))
            }
            is EntityHitResult -> {
                // TODO: determine which model part the ray has hit

                val entity = hit.entity ?: return
                val id = Registry.ENTITY_TYPE.getId(entity.type).toString()

                val colorDefault = when (entity) {
                    is HostileEntity -> Lidar.config.entityColorHostile
                    is PassiveEntity -> Lidar.config.entityColorPeaceful
                    else -> Lidar.config.entityColorDefault
                }
                val color = getColor(id, Lidar.config.entityColorMap, colorDefault) ?: return

                addParticle(
                    world = world,
                    pos = hit.pos,
                    info = DotParticle.Info(
                        color = color,
                        entityId = entity.id,
                        entityOffset = entity.pos.subtract(hit.pos),
                        entityPart = 0,
                    ),
                )
            }
        }
    }

    fun addParticle(world: ClientWorld, pos: Vec3d, info: DotParticle.Info) {
        val (i, j, k) = info.encode()

        world.addImportantParticle(
            DotParticle.DOT,
            pos.x, pos.y, pos.z,
            i, j, k,
        )
    }

}