package me.jfenn.lidar.data

import me.jfenn.lidar.Lidar
import me.jfenn.lidar.MOD_ID
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.client.particle.*
import net.minecraft.client.world.ClientWorld
import net.minecraft.particle.DefaultParticleType
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry

class DotParticle(
    val clientWorld: ClientWorld,
    x: Double,
    y: Double,
    z: Double,
    colorTmp: Double,
    entityIdTmp: Double,
    entityOffsetTmp: Double,
) : AbstractSlowingParticle(clientWorld, x, y, z, 0.0, 0.0, 0.0) {

    private val info = Info.decode(colorTmp, entityIdTmp, entityOffsetTmp)
    private val blockPos = BlockPos(x, y, z)

    init {
        // set max age from config
        maxAge = MAX_AGE
        // set scale = 0.2
        scale = 0.01f
        // set color based on provided color hex int
        red = info.red
        green = info.green
        blue = info.blue
    }

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    override fun move(dx: Double, dy: Double, dz: Double) {
        if (info.entityId != null && info.entityOffset != null) {
            val entity = clientWorld.getEntityById(info.entityId)
            if (entity == null || entity.isRemoved) {
                markDead()
                return
            }

            // TODO: based on provided entity part id, apply rotations to entityOffset

            // Calculate a new particle position from the original entityOffset
            val newPos = entity.pos.subtract(info.entityOffset);
            x = newPos.x
            y = newPos.y
            z = newPos.z
        } else {
            val block = clientWorld.getBlockState(blockPos)
            if (block.isAir) {
                markDead()
                return
            }
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
        val entityPart: Int? = null,
        val entityOffset: Vec3d? = null,
    ) {

        val red = (color shr 16 and 0xFF) / 255f
        val green = (color shr 8 and 0xFF) / 255f
        val blue = (color and 0xFF) / 255f

        fun encode(): Triple<Double, Double, Double> {
            return Triple(
                Double.fromBits(color.toLong()),
                if (entityId != null && entityPart != null) {
                    Double.fromBits(
                        (entityId.toLong() shl 32)
                            .or(entityPart.toLong())
                    )
                } else Double.fromBits(0L),
                entityOffset?.let{
                    Double.fromBits(
                        (((it.x * 32).toLong().coerceIn(-127, 128) + 127) shl 16)
                            .or(((it.y * 32).toLong().coerceIn(-127, 128) + 127) shl 8)
                            .or((it.z * 32).toLong().coerceIn(-127, 128) + 127)
                    )
                } ?: Double.fromBits(0L),
            )
        }

        companion object {
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
                val entityPart = entityIdTmp.takeIf { isEntity }?.toBits()?.let {
                    (it and 0xFFFFFFFF).toInt()
                }
                val entityOffset = entityOffsetTmp.toBits().let {
                    Vec3d(
                        ((it ushr 16 and 0xFF) - 127.0) / 32.0,
                        ((it ushr 8 and 0xFF) - 127.0) / 32.0,
                        ((it and 0xFF) - 127.0) / 32.0,
                    )
                }

                return Info(color, entityId, entityPart, entityOffset)
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
        val MAX_AGE by lazy {
            Lidar.config.lidarDuration
        }

        fun register() = Registry.register(Registry.PARTICLE_TYPE, ID, DOT)

        fun registerClient() {
            ParticleFactoryRegistry.getInstance().register(DOT) { it -> Factory(it) }
        }
    }
}
