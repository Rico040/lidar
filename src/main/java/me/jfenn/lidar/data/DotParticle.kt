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
    private val clientWorld: ClientWorld,
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
        if (info.entityId != null) {
            // Don't add any behavior for entity IDs
            return
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
                Double.fromBits(0L),
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

                return Info(color, entityId)
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
