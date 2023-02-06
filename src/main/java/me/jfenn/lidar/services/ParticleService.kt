package me.jfenn.lidar.services

import me.jfenn.lidar.Lidar
import me.jfenn.lidar.data.DotParticle
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry

object ParticleService {

    private const val OFFSET_MIN = 0.0
    private const val OFFSET_MAX = 0.99

    private fun String.parseColor(): Int? {
        return removePrefix("#").toIntOrNull(16)
    }

    fun getBlockColor(blockState: BlockState): Int? {
        if (blockState.isAir) return null

        val id = Registry.BLOCK.getId(blockState.block).toString()
        val colorString = Lidar.config.blockColorMap.let {
            it[id] ?: it["default"]
        }

        return colorString?.parseColor()
    }

    fun addBlockHit(hit: BlockHitResult) {
        val world = MinecraftClient.getInstance().world ?: return

        val blockState = world.getBlockState(hit.blockPos) ?: return
        val color = getBlockColor(blockState) ?: return

        val offset = hit.pos.subtract(hit.blockPos.x.toDouble(), hit.blockPos.y.toDouble(), hit.blockPos.z.toDouble())

        val pos = Vec3d(
            (hit.blockPos.x + offset.x.coerceIn(OFFSET_MIN, OFFSET_MAX)),
            (hit.blockPos.y + offset.y.coerceIn(OFFSET_MIN, OFFSET_MAX)),
            (hit.blockPos.z + offset.z.coerceIn(OFFSET_MIN, OFFSET_MAX)),
        )

        addParticle(world, pos, DotParticle.Info(color))
    }

    fun getEntityColor(entity: Entity): Int? {
        val id = Registry.ENTITY_TYPE.getId(entity.type).toString()
        val type = when (entity) {
            is HostileEntity -> "hostile"
            is PassiveEntity -> "passive"
            else -> "neutral"
        }
        val colorString = Lidar.config.entityColorMap.let {
            it[id] ?: it[type] ?: it["default"]
        }

        return colorString?.parseColor()
    }

    fun addEntityHit(hit: EntityHitResult, hitPos: Vec3d) {
        val world = MinecraftClient.getInstance().world ?: return
        val entity = hit.entity ?: return
        val color = getEntityColor(hit.entity) ?: return

        addParticle(
            world = world,
            pos = hitPos,
            info = DotParticle.Info(
                color = color,
                entityId = entity.id,
            ),
        )
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