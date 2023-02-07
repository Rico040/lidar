package me.jfenn.lidar.data

import me.jfenn.lidar.Lidar.config
import me.jfenn.lidar.MOD_ID
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.client.particle.*
import net.minecraft.client.world.ClientWorld
import net.minecraft.particle.DefaultParticleType
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry

class DotParticle(
    clientWorld: ClientWorld,
    x: Double,
    y: Double,
    z: Double,
    colorTmp: Double,
    entityIdTmp: Double,
    entityOffsetTmp: Double,
) : AbstractSlowingParticle(clientWorld, x, y, z, 0.0, 0.0, 0.0) {

    private val info = Info.decode(colorTmp, entityIdTmp, entityOffsetTmp)
    private val blockPos = BlockPos(x, y, z)

    private val blockIdentity = run {
        world.getBlockState(blockPos)
    }.let {
        System.identityHashCode(it)
    }

    init {
        // set max age from config
        maxAge = if (info.entityId != null) config.lidarDurationEntity else config.lidarDurationBlock
        // set scale = 0.2
        scale = 0.01f
        // set color based on provided color hex int
        red = info.red
        green = info.green
        blue = info.blue
    }

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    override fun move(dx: Double, dy: Double, dz: Double) {
        if (info.entityId == null) {
            // if the particle is a block, don't do any transforms...
            val block = world.getBlockState(blockPos)
            if (block.isAir) markDead()
            if (System.identityHashCode(block) != blockIdentity) markDead()
            return
        }

        if (!config.entityParticleFollow) {
            // Don't add any behavior for entity IDs
            return
        }

        val entity = world.getEntityById(info.entityId)
        if (entity == null || entity.isRemoved) {
            // entity has been killed, or is out of view
            markDead()
            return
        }

        // Calculate a new particle position from the original entityOffset
        if (info.entityOffset != null) {
            val newPos = info.entityOffset.rotateY(entity.bodyYaw)
                .add(entity.pos)

            x = newPos.x
            y = newPos.y
            z = newPos.z
        }
    }

    override fun getSize(tickDelta: Float): Float {
        val f = (age.toFloat() + tickDelta) / maxAge.toFloat()
        return scale * (1.0f - f * f * 0.5f)
    }

    override fun getBrightness(tint: Float): Int {
        val f = ((age.toFloat() + tint) / maxAge.toFloat()).coerceIn(0f, 1f)
        val i = 15728880
        val j = ((i and 255) + (f * 15.0f * 16.0f).toInt()).coerceAtMost(240)
        val k = i shr 16 and 255
        return j or (k shl 16)
    }

    class Info(
        val color: Int,
        val entityId: Int? = null,
        val entityOffset: Vec3d? = null,
    ) {

        val red = (color shr 16 and 0xFF) / 255f
        val green = (color shr 8 and 0xFF) / 255f
        val blue = (color and 0xFF) / 255f

        fun encode(): Triple<Double, Double, Double> {
            return Triple(
                Double.fromBits(color.toLong()),
                if (entityId != null) {
                    Double.fromBits(
                        (entityId.toLong() shl 32)
                    )
                } else Double.fromBits(0L),
                entityOffset?.let {
                    Double.fromBits(it.toBits())
                } ?: Double.fromBits(0L),
            )
        }

        companion object {

            private const val VEC3D_SCALE = 128.0
            private const val VEC3D_MIN = -4096L
            private const val VEC3D_MAX = 4096L

            private fun Vec3d.toBits(): Long {
                return (((x * VEC3D_SCALE).toLong().coerceIn(VEC3D_MIN, VEC3D_MAX) - VEC3D_MIN) shl 42)
                    .or(((y * VEC3D_SCALE).toLong().coerceIn(VEC3D_MIN, VEC3D_MAX) - VEC3D_MIN) shl 21)
                    .or((z * VEC3D_SCALE).toLong().coerceIn(VEC3D_MIN, VEC3D_MAX) - VEC3D_MIN)
            }

            private fun vec3dFromBits(bits: Long): Vec3d {
                return Vec3d(
                    ((bits ushr 42 and 0xFFFF) + VEC3D_MIN) / VEC3D_SCALE,
                    ((bits ushr 21 and 0xFFFF) + VEC3D_MIN) / VEC3D_SCALE,
                    ((bits and 0xFFFF) + VEC3D_MIN) / VEC3D_SCALE,
                )
            }

            fun decode(
                colorTmp: Double,
                entityIdTmp: Double,
                entityOffsetTmp: Double,
            ): Info {
                val color = colorTmp.toBits().toInt()
                val isEntity = entityIdTmp != Double.fromBits(0L)
                val entityId = entityIdTmp.takeIf { isEntity }?.toBits()?.let {
                    (it ushr 32 and 0xFFFFFFFF).toInt()
                }
                val entityOffset = entityOffsetTmp.takeIf { isEntity }?.toBits()?.let {
                    vec3dFromBits(it)
                }

                return Info(color, entityId, entityOffset)
            }
        }

    }

    class Factory(private val spriteProvider: SpriteProvider) : ParticleFactory<DefaultParticleType?> {
        override fun createParticle(
            defaultParticleType: DefaultParticleType?,
            clientWorld: ClientWorld,
            d: Double,
            e: Double,
            f: Double,
            g: Double,
            h: Double,
            i: Double
        ): Particle {
            val flameParticle = DotParticle(clientWorld, d, e, f, g, h, i)
            flameParticle.setSprite(spriteProvider)
            return flameParticle
        }
    }

    companion object {
        val DOT = FabricParticleTypes.simple()!!
        val ID = Identifier(MOD_ID, "dot")

        fun register() = Registry.register(Registry.PARTICLE_TYPE, ID, DOT)

        fun registerClient() {
            ParticleFactoryRegistry.getInstance().register(DOT) { it -> Factory(it) }
        }
    }
}
